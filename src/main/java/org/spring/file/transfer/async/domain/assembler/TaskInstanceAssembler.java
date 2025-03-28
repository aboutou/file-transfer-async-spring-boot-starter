package org.spring.file.transfer.async.domain.assembler;

import org.spring.file.transfer.async.domain.entities.TaskInstance;
import org.spring.file.transfer.async.domain.entities.model.Req;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @author tiny
 * 
 * @since 2023/5/18 上午11:44
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, implementationPackage = "<PACKAGE_NAME>.impl")
public interface TaskInstanceAssembler {

    TaskInstance convert(Req req);

}
