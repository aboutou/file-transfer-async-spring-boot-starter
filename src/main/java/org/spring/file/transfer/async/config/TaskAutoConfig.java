package org.spring.file.transfer.async.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.spring.file.transfer.async.core.FileContentConverter;
import org.spring.file.transfer.async.core.I18nHandler;
import org.spring.file.transfer.async.core.TaskHandler;
import org.spring.file.transfer.async.core.batchoprate.handle.BatchOprateTaskHandler;
import org.spring.file.transfer.async.core.batchoprate.service.BatchOprateTaskService;
import org.spring.file.transfer.async.core.export.handle.AbstractFileExportHandler;
import org.spring.file.transfer.async.core.export.handle.AsyncFileExportHandlerImpl;
import org.spring.file.transfer.async.core.export.service.TaskFileExportService;
import org.spring.file.transfer.async.core.impl.HttpMultipartFormDataFileContentConverterImpl;
import org.spring.file.transfer.async.core.imports.handle.AsnycFileImportHandlerImpl;
import org.spring.file.transfer.async.core.imports.service.TaskFileImportService;
import org.spring.file.transfer.async.domain.factory.TaskInstanceFactory;
import org.spring.file.transfer.async.domain.repository.impl.InMemoryTaskInstanceRepositoryImpl;
import org.spring.file.transfer.async.domain.service.SpringContextService;
import org.spring.file.transfer.async.domain.service.impl.DefaultTaskIdGenerator;
import org.spring.file.transfer.async.services.TaskService;
import org.spring.file.transfer.async.services.impl.TaskServiceImpl;
import org.spring.file.transfer.async.web.rest.TaskController;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;

/**
 * @author tiny
 * 
 * @since 2023/5/17 下午6:00
 */
@Configuration
@Import({EventConfig.class, OpenCsvConfig.class, EasyPoiConfig.class})
public class TaskAutoConfig {


    public static final String TASK_FILE_TASK_REPOSITORY_NAME = "taskFileTaskRepositoryName";
    public static final String TASK_ID_GENERATOR_NAME = "taskIdGeneratorName";

    @Bean
    public SpringContextService springContextService() {
        return new SpringContextService();
    }

    @Bean
    @ConditionalOnMissingBean
    public I18nHandler i18nHandler() {
        return (n) -> n;
    }

    @Bean(TASK_FILE_TASK_REPOSITORY_NAME)
    @ConditionalOnMissingBean(name = TASK_FILE_TASK_REPOSITORY_NAME)
    public InMemoryTaskInstanceRepositoryImpl taskRepository() {
        return new InMemoryTaskInstanceRepositoryImpl();
    }

    @Bean
    public TaskService taskService(List<TaskHandler> taskHandlers) {
        return new TaskServiceImpl(taskHandlers);
    }

    @Bean
    public TaskController taskController(TaskService taskService) {
        return new TaskController(taskService);
    }

    @Bean
    public TaskInstanceFactory taskInstanceFactory() {
        return new TaskInstanceFactory();
    }

    @Bean(TASK_ID_GENERATOR_NAME)
    @ConditionalOnMissingBean(name = TASK_ID_GENERATOR_NAME)
    public DefaultTaskIdGenerator taskIdGenerator() {
        return new DefaultTaskIdGenerator();
    }

    @Bean
    public HttpMultipartFormDataFileContentConverterImpl httpMultipartFormDataFileContentConverter() {
        return new HttpMultipartFormDataFileContentConverterImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public AsnycFileImportHandlerImpl asnycFileImportHandlerImpl(List<TaskFileImportService> taskFileImportService,
                                                                 List<FileContentConverter> fileContentConverters,
                                                                 ObjectMapper objectMapper) {
        AsnycFileImportHandlerImpl asnycFileImportHandler = new AsnycFileImportHandlerImpl(taskFileImportService, fileContentConverters);
        asnycFileImportHandler.setObjectMapper(objectMapper);
        return asnycFileImportHandler;
    }

    @Bean
    @ConditionalOnMissingBean
    public AbstractFileExportHandler abstractFileExportHandler(List<TaskFileExportService> taskFileImportService,
                                                               List<FileContentConverter> fileContentConverters,
                                                               ObjectMapper objectMapper) {
        AsyncFileExportHandlerImpl asyncFileExportHandler = new AsyncFileExportHandlerImpl(taskFileImportService, fileContentConverters);
        asyncFileExportHandler.setObjectMapper(objectMapper);
        return asyncFileExportHandler;
    }

    @Bean
    @ConditionalOnMissingBean
    public BatchOprateTaskHandler batchOprateTaskHandler(List<BatchOprateTaskService> batchOprateTaskService,
                                                         ObjectMapper objectMapper) {
        BatchOprateTaskHandler batchOprateTaskHandler = new BatchOprateTaskHandler(batchOprateTaskService);
        batchOprateTaskHandler.setObjectMapper(objectMapper);
        return batchOprateTaskHandler;
    }
}
