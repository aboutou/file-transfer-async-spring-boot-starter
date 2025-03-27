package org.spring.file.transfer.async.web.rest;

import org.spring.file.transfer.async.commons.BizType;
import org.spring.file.transfer.async.commons.TaskType;
import org.spring.file.transfer.async.domain.assembler.ResAssembler;
import org.spring.file.transfer.async.domain.entities.model.Req;
import org.spring.file.transfer.async.domain.entities.model.Res;
import org.spring.file.transfer.async.services.TaskService;
import org.spring.file.transfer.async.utils.CommonResult;
import org.spring.file.transfer.async.web.dto.input.ExportTaskInput;
import org.spring.file.transfer.async.web.dto.input.ImportTaskInput;
import org.spring.file.transfer.async.web.dto.output.AsyncTaskResultOutput;

import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.mapstruct.factory.Mappers;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 导入导出
 *
 * @author bm
 */
@Slf4j
@ResponseBody
@AllArgsConstructor
@RequestMapping(value = "/web/task")
public class TaskController {


    private final TaskService taskService;
    private static final ResAssembler resAssembler = Mappers.getMapper(ResAssembler.class);

    /**
     * 异步导入任务
     *
     * @param input
     * @return
     */
    @ApiOperation("异步导入任务")
    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public CommonResult<AsyncTaskResultOutput> importTaskMu(MultipartHttpServletRequest request, @Valid ImportTaskInput input) {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (Objects.nonNull(requestAttributes)) {
            HttpServletRequest request1 = requestAttributes.getRequest();
            if (request != request1) {
                HttpServletResponse response1 = requestAttributes.getResponse();
                ServletRequestAttributes attributes = new ServletRequestAttributes(request, response1);
                RequestContextHolder.setRequestAttributes(attributes);
            }
        }
        // log.info("{}aaaa {}", request, request.getClass());
        Req<String> req = new Req<>();
        req.setTaskType(TaskType.FILE_IMPORT);
        req.setBizType(new BizType.DefaultBizType(input.getBizType()));
        req.setAsync(input.getAsync());
        req.setTaskParam(input.getTaskParam());
        Res res = taskService.execute(req);
        return CommonResult.success(resAssembler.convert(res));
    }

    /**
     * 异步导入任务
     *
     * @param input
     * @return
     */
    @ApiOperation("异步导入任务")
    @PostMapping(value = "/import", consumes = MediaType.APPLICATION_JSON_VALUE)
    public CommonResult<AsyncTaskResultOutput> importTask(@RequestBody @Valid ImportTaskInput input) {
        Req<String> req = new Req<>();
        req.setTaskType(TaskType.FILE_IMPORT);
        req.setBizType(new BizType.DefaultBizType(input.getBizType()));
        req.setAsync(input.getAsync());
        req.setTaskParam(input.getTaskParam());
        Res res = taskService.execute(req);
        return CommonResult.success(resAssembler.convert(res));
    }


    /**
     * 异步导入任务查询
     *
     * @param id
     * @param bizType
     * @return
     */
    @GetMapping("/import/result")
    @ApiOperation("异步导入任务")
    public CommonResult<AsyncTaskResultOutput> importTaskResult(@RequestParam(value = "id") @NotBlank(message = "id不能为空") String id,
                                                                @RequestParam(value = "bizType") @NotBlank(message = "bizType不能为空") String bizType) {
        Res res = taskService.find(TaskType.FILE_IMPORT, bizType, id);
        return CommonResult.success(resAssembler.convert(res));
    }

    /**
     * 导入任务删除
     *
     * @param id
     * @param bizType
     * @return
     */
    @DeleteMapping("/remove/import")
    @ApiOperation("导入任务删除")
    public CommonResult<Boolean> delImportTask(@RequestParam(value = "id") @NotBlank(message = "id不能为空") String id,
                                               @RequestParam(value = "bizType") @NotBlank(message = "bizType不能为空") String bizType) {
        taskService.remove(TaskType.FILE_IMPORT, bizType, id);
        return CommonResult.success(true);
    }

    /**
     * 异步导出任务
     *
     * @param input
     * @return
     */
    @PostMapping("/export")
    @ApiOperation("异步导出任务")
    public CommonResult<AsyncTaskResultOutput> exportTask(@RequestBody @Valid ExportTaskInput input) {
        Req<String> req = new Req<>();
        req.setTaskType(TaskType.FILE_EXPORT);
        req.setBizType(new BizType.DefaultBizType(input.getBizType()));
        req.setAsync(input.getAsync());
        req.setTaskParam(input.getTaskParam());
        Res res = taskService.execute(req);
        AsyncTaskResultOutput convert = resAssembler.convert(res);
        return CommonResult.success(convert);
    }


    /**
     * 异步导出任务查询
     *
     * @param id
     * @param bizType
     * @return
     */
    @GetMapping("/export/result")
    @ApiOperation("异步导出任务查询")
    public CommonResult<AsyncTaskResultOutput> exportTaskResult(@RequestParam(value = "id") @NotBlank(message = "id不能为空") String id,
                                                                @RequestParam(value = "bizType") @NotBlank(message = "bizType不能为空") String bizType) {
        Res res = taskService.find(TaskType.FILE_EXPORT, bizType, id);
        return CommonResult.success(resAssembler.convert(res));
    }

    /**
     * 导出任务删除
     *
     * @param id
     * @param bizType
     * @return
     */
    @DeleteMapping("/remove/export")
    @ApiOperation("导出任务删除")
    public CommonResult<Boolean> delExportTask(@RequestParam(value = "id") @NotBlank(message = "id不能为空") String id,
                                               @RequestParam(value = "bizType") @NotBlank(message = "bizType不能为空") String bizType) {
        taskService.remove(TaskType.FILE_EXPORT, bizType, id);
        return CommonResult.success(true);
    }

    /**
     * 异步批量操作任务
     *
     * @param input
     * @return
     */
    @PostMapping("/batch")
    @ApiOperation("异步导出任务")
    public CommonResult<AsyncTaskResultOutput> batchTask(@RequestBody @Valid ExportTaskInput input) {
        Req<String> req = new Req<>();
        req.setTaskType(TaskType.BATCH_OPERATION);
        req.setBizType(new BizType.DefaultBizType(input.getBizType()));
        req.setAsync(input.getAsync());
        req.setTaskParam(input.getTaskParam());
        Res res = taskService.execute(req);
        AsyncTaskResultOutput convert = resAssembler.convert(res);
        return CommonResult.success(convert);
    }


    /**
     * 异步批量操作任务查询
     *
     * @param id
     * @param bizType
     * @return
     */
    @GetMapping("/batch/result")
    @ApiOperation("异步导出任务查询")
    public CommonResult<AsyncTaskResultOutput> batchTaskResult(@RequestParam(value = "id") @NotBlank(message = "id不能为空") String id,
                                                               @RequestParam(value = "bizType") @NotBlank(message = "bizType不能为空") String bizType) {
        Res res = taskService.find(TaskType.BATCH_OPERATION, bizType, id);
        return CommonResult.success(resAssembler.convert(res));
    }

    /**
     * 批量操作任务删除
     *
     * @param id
     * @param bizType
     * @return
     */
    @DeleteMapping("/remove/batch")
    @ApiOperation("导出任务删除")
    public CommonResult<Boolean> delBatchTask(@RequestParam(value = "id") @NotBlank(message = "id不能为空") String id,
                                              @RequestParam(value = "bizType") @NotBlank(message = "bizType不能为空") String bizType) {
        taskService.remove(TaskType.BATCH_OPERATION, bizType, id);
        return CommonResult.success(true);
    }

    /**
     * 所有任务查询
     *
     * @return
     */
    @GetMapping("/all/task")
    @ApiOperation("异步任务查询")
    public CommonResult<List<AsyncTaskResultOutput>> allTask() {
        List<Res> all = taskService.findAll();
        if (CollectionUtils.isEmpty(all)) {
            return CommonResult.success(new ArrayList<>());
        }
        List<AsyncTaskResultOutput> resLists = new ArrayList<>();
        all.removeIf(res -> {
            AsyncTaskResultOutput convert = resAssembler.convert(res);
            resLists.add(convert);
            return true;
        });
        return CommonResult.success(resLists);
    }

}
