package org.spring.file.transfer.async.core.imports.handle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.phantomthief.pool.KeyAffinityExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.spring.file.transfer.async.commons.*;
import org.spring.file.transfer.async.core.AbstractTaskFileService;
import org.spring.file.transfer.async.core.AbstractTaskHandler;
import org.spring.file.transfer.async.core.FileContentConverter;
import org.spring.file.transfer.async.core.exception.TaskException;
import org.spring.file.transfer.async.core.imports.model.FileContentModel;
import org.spring.file.transfer.async.core.imports.model.ImportFailResult;
import org.spring.file.transfer.async.core.imports.model.ImportResult;
import org.spring.file.transfer.async.core.imports.service.TaskFileImportService;
import org.spring.file.transfer.async.domain.entities.TaskFailResultInstance;
import org.spring.file.transfer.async.domain.entities.TaskInstance;
import org.spring.file.transfer.async.domain.entities.model.Req;
import org.spring.file.transfer.async.domain.events.TaskChanagedEvent;
import org.spring.file.transfer.async.domain.handle.TaskErrorHandler;
import org.spring.file.transfer.async.domain.repository.TaskRepository;
import org.spring.file.transfer.async.utils.FastJsonUtil;
import org.spring.file.transfer.async.utils.SpringContextHolderUtil;
import org.spring.file.transfer.async.web.dto.param.ImportTaskParam;
import org.springframework.http.HttpInputMessage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author tiny
 * @apiNote
 * @since 2023/5/12 下午2:49
 */
@Slf4j
public class AsnycFileImportHandlerImpl extends AbstractTaskHandler<String, String> {


    private final List<TaskFileImportService> taskFileImportServices;
    private final List<FileContentConverter> fileContentConverters;
    private ObjectMapper objectMapper;

    public AsnycFileImportHandlerImpl(List<TaskFileImportService> taskFileImportService,
                                      List<FileContentConverter> fileContentConverters) {
        this.taskFileImportServices = taskFileImportService;
        this.fileContentConverters = fileContentConverters;
    }


    @Override
    public TaskInstance<String> doExcute(Req<String> req) {
        for (TaskFileImportService taskService : taskFileImportServices) {
            BizType bizType = taskService.getBizType();
            String bizCode = Optional.ofNullable(req).map(Req::getBizType).map(BizType::getCode).orElse(null);
            boolean biz = Optional.ofNullable(bizType).map(BizType::getCode).map(p -> StringUtils.equalsIgnoreCase(p, bizCode)).orElse(false);
            if (!biz) {
                continue;
            }

            String taskParam = req.getTaskParam();
            //ObjectMapper objectMapper = Optional.ofNullable(getObjectMapper()).orElseGet(() -> FastJsonUtil.getObjectMapper());
            ObjectMapper objectMapper = Optional.ofNullable(getObjectMapper()).orElseGet(() -> FastJsonUtil.getObjectMapper());
            // obj = FastJsonUtil.parseObject(taskParam, paramClass, objectMapper);
            ImportTaskParam obj = (ImportTaskParam) FastJsonUtil.parseObject(taskParam, taskService.getParamClass(), objectMapper);
            taskService.preConditions(obj);

            boolean supportAsync = taskService.isSupportAsync();
            boolean async = Boolean.TRUE.equals(req.getAsync());
            TaskRepository taskRepository = taskService.getTaskRepository();
            Serializable taskId = taskService.getTaskIdGenerator().generateId(req.getTaskType());
            TaskInstance taskInstance = taskService.getTaskInstance(req);
            taskInstance.setTaskId(taskId);
            taskInstance.setAsyncFlag(async && supportAsync ? 1 : 0);
            taskInstance.setWaitResultFlag(taskService.waitResult() ? 1 : 0);
            taskInstance.setErrorShowType(taskService.errorShowType());
            if (async && supportAsync && taskService.waitResult()) {
                taskInstance.setMaxPollingTimes(taskService.getMaxPollingTimes());
            }
            taskInstance.setTaskState(TaskState.新建);
            BizType b = new BizType() {

                @Override
                public String getCode() {
                    return bizType.getCode();
                }

                @Override
                public String getName() {
                    List<String> names = new ArrayList<>();
                    CollectionUtils.addIgnoreNull(names, bizType.getName());
                    CollectionUtils.addIgnoreNull(names, obj.getFileName());
                    //StringUtils.joinWith("-", bizType.getName(), obj.getFileName());
                    return StringUtils.join(names, "-");
                }

                @Override
                public List<String> errFieldOrder() {
                    return bizType.errFieldOrder();
                }

                @Override
                public List<String> ignoreField() {
                    return bizType.ignoreField();
                }

            };

            taskInstance.setBizType(new BizType.DefaultBizType(b));
            taskRepository.save(taskInstance);


            String validateParams = taskService.validateParams(obj);
            if (StringUtils.isNotBlank(validateParams)) {
                TaskChanagedEvent event = new TaskChanagedEvent(taskRepository, taskId, false, getTaskType());
                event.setTaskState(TaskState.失败);
                event.setFailReason("导入参数验证失败：" + validateParams);
                event.setErrorShowType(ErrorShowType.TOAST);
                SpringContextHolderUtil.publishEvent(event);
                return taskRepository.find(TaskType.FILE_IMPORT, taskId);
            }
            if (async && supportAsync) {
                KeyAffinityExecutor executorService = taskService.getExecutorService();
                String hashKey = Optional.ofNullable(taskService.getTaskDataPartitionValue()).map(p -> p.getHashDataPartitionValue(getTaskType())).map(String::valueOf).orElse(bizType.getCode());
                ((AbstractTaskFileService) taskService).handleExportDataProgress(taskId, TaskState.排队中, true);
                executorService.submit(hashKey, (() -> {
                    excuteImport(obj, taskService, taskId, true);
                    return taskId;
                }));
            } else {
                ((AbstractTaskFileService) taskService).handleExportDataProgress(taskId, TaskState.排队中, false);
                excuteImport(obj, taskService, taskId, false);
            }
            return taskRepository.find(TaskType.FILE_IMPORT, taskId);
        }
        return null;
    }

    @Override
    public List<TaskFileImportService> getTaskFileService() {
        return taskFileImportServices;
    }

    public void excuteImport(ImportTaskParam param, TaskFileImportService taskFileImportService, Serializable taskId, boolean async) {
        boolean hasError = false;
        Throwable ec = null;
        try {
            ((AbstractTaskFileService) taskFileImportService).handleExportDataProgress(taskId, TaskState.执行中, async);
            taskFileImportService.handleTaskBefore(taskId, param);
            excuteImport0(param, taskFileImportService, taskId, async);
            taskFileImportService.handleTaskSuccess(taskId, param);
        } catch (TaskException e) {
            ec = e;
            log.info("导入文件报错：" + taskFileImportService.getBizType() + ", 错误信息为" + e.getMessage(), e);
            ((AbstractTaskFileService) taskFileImportService).handleExportDataProgress(taskId, e.getErrorShowType(), e.getFailReason(), async);
            taskFileImportService.handleTaskException(taskId, param, e);
        } catch (Throwable e) {
            ec = e;
            hasError = true;
            log.error("导入文件报错：" + taskFileImportService.getBizType() + ", 错误信息为" + e.getMessage(), e);
            TaskErrorHandler taskErrorHandler = taskFileImportService.getTaskErrorHandler();
            if (taskErrorHandler != null) {
                Throwable throwable = taskErrorHandler.handleError(e, taskId, taskFileImportService.getBizType());
                if (throwable instanceof TaskException) {
                    ((AbstractTaskFileService) taskFileImportService).handleExportDataProgress(taskId, ((TaskException) throwable).getErrorShowType(), ((TaskException) throwable).getFailReason(), async);
                    hasError = false;
                }
            }
            taskFileImportService.handleTaskException(taskId, param, e);
        } finally {
            if (hasError) {
                ((AbstractTaskFileService) taskFileImportService).handleExportDataProgress(taskId, ErrorShowType.TOAST, "导入文件任务处理出错", async);
            }
            taskFileImportService.handleTaskComplete(taskId, param, ec);
        }
    }

    public void excuteImport0(ImportTaskParam param, TaskFileImportService taskFileImportService, Serializable taskId, boolean async) {

        ((AbstractTaskFileService) taskFileImportService).handleExportDataProgress(taskId, TaskState.文件获取中, async);

        HttpInputMessage httpInputMessage = getHttpInputMessage(param);
        {
            if (httpInputMessage == null) {
                TaskChanagedEvent event = new TaskChanagedEvent(taskFileImportService.getTaskRepository(), taskId, async, getTaskType());
                event.setTaskState(TaskState.失败);
                event.setFailReason("文件内容无法获取,请按模板文件进行导入");
                event.setErrorShowType(ErrorShowType.TOAST);
                SpringContextHolderUtil.publishEvent(event);
                return;
            }
        }
        ((AbstractTaskFileService) taskFileImportService).handleExportDataProgress(taskId, TaskState.文件解析中, async);
        // 解析文件
        List dataList0 = taskFileImportService.fileParsing(param, httpInputMessage);
        List dataList = taskFileImportService.preProcessingData(dataList0, param);
        int totalSize = CollectionUtils.size(dataList);
        {
            TaskChanagedEvent event = new TaskChanagedEvent(taskFileImportService.getTaskRepository(), taskId, async, getTaskType());
            event.setTotalNum((long) totalSize);
            // 文件解析失败
            if (totalSize == 0) {
                event.setTaskState(TaskState.失败);
                event.setFailReason("文件解析不到数据,请按模板文件进行导入");
                event.setErrorShowType(ErrorShowType.TOAST);
                SpringContextHolderUtil.publishEvent(event);
                return;
            }
            event.setTaskState(TaskState.执行中);
            SpringContextHolderUtil.publishEvent(event);
        }

        ((AbstractTaskFileService) taskFileImportService).handleExportDataProgress(taskId, TaskState.数据验证中, async);
        // 数据验证
        List<TaskFailResultInstance> failResults = new ArrayList<>();
        ImportResult validate = taskFileImportService.validate(param, dataList);
        long failSize = CollectionUtils.size(validate.getFailList());
        if (failSize > 0) {
            List<ImportFailResult> failList = validate.getFailList();
            List<TaskFailResultInstance> collect = failList.stream().map(p -> getTaskFailResultInstance(p)).collect(Collectors.toList());
            CollectionUtils.addAll(failResults, collect);
        }
        List successList = validate.getSuccessList();
        int successSize = CollectionUtils.size(successList);
        {
            TaskChanagedEvent event = new TaskChanagedEvent(taskFileImportService.getTaskRepository(), taskId, async, getTaskType());
            event.setFailNum(failSize);
            // event.setSucessNum(successSize);
            // 返回的数据异常的情况
            if (successSize + failSize != totalSize) {
                event.setTaskState(TaskState.失败);
                event.setFailReason("文件解析不到数据,请按模板文件进行导入");
                event.setFailResults(failResults);
                event.setErrorShowType(ErrorShowType.TOAST);
                SpringContextHolderUtil.publishEvent(event);
                return;
            }
            // 全部校验失败的情况
            if (successSize == 0) {
                event.setTaskState(TaskState.执行完成);
                event.setFailResults(failResults);
                SpringContextHolderUtil.publishEvent(event);
                return;
            }
            // 部分校验失败，部分校验成功的情况，记录错误信息
            if (failSize > 0) {
                event.setTaskState(TaskState.执行中);
                event.setFailResults(failResults);
                SpringContextHolderUtil.publishEvent(event);
            }
        }

        // 结果返回模式
        DataResultMode dataResultMode = taskFileImportService.dataResultMode();
        {
            if (DataResultMode.ALL.equals(dataResultMode) && failSize > 0) {
                TaskChanagedEvent event = new TaskChanagedEvent(taskFileImportService.getTaskRepository(), taskId, async, getTaskType());
                event.setTaskState(TaskState.执行完成);
                SpringContextHolderUtil.publishEvent(event);
                return;
            }
        }
        // 执行导入
        ((AbstractTaskFileService) taskFileImportService).handleExportDataProgress(taskId, TaskState.数据处理中, async);
        ImportResult importResult = taskFileImportService.dataHandle(taskId, param, successList);
        // 处理结果
        {
            // 忽略成功，只处理错误信息
            // List successList = importResult.getSuccessList();
            List<ImportFailResult> failList = importResult.getFailList();
            long resultFailSize = CollectionUtils.size(failList);
            TaskChanagedEvent event = new TaskChanagedEvent(taskFileImportService.getTaskRepository(), taskId, async, getTaskType());
            long resultSuccessSize = successSize - resultFailSize;
            event.setSucessNum(resultSuccessSize);
            event.setFailNum(resultFailSize);
            event.setTaskState(TaskState.执行完成);
            event.setFullSucessNum(true);
            if (resultFailSize > 0) {
                List<TaskFailResultInstance> collect = failList.stream().map(p -> getTaskFailResultInstance(p)).collect(Collectors.toList());
                CollectionUtils.addAll(failResults, collect);
            }
            if (resultFailSize > 0) {
                event.setFailResults(failResults);
            }
            SpringContextHolderUtil.publishEvent(event);
            return;
        }
    }


    private TaskFailResultInstance getTaskFailResultInstance(ImportFailResult result) {
        TaskFailResultInstance resultInstance = new TaskFailResultInstance(result);
        return resultInstance;
    }

    private HttpInputMessage getHttpInputMessage(ImportTaskParam param) {
        for (FileContentConverter fileContentConverter : fileContentConverters) {
            if (fileContentConverter.supports(param.getFileContentType())) {
                FileContentModel fileContent0 = new FileContentModel();
                fileContent0.setFileContent(param.getFileContent());
                fileContent0.setFileContentType(param.getFileContentType());
                return fileContentConverter.remoteConvert(fileContent0);
            }
        }
        return null;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.FILE_IMPORT;
    }

    @Override
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}