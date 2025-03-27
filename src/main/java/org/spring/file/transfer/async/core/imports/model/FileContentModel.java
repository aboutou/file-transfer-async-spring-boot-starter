package org.spring.file.transfer.async.core.imports.model;


import org.spring.file.transfer.async.commons.FileContentType;
import org.spring.file.transfer.async.commons.FileFormat;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;

/**
 * @author wuzhencheng
 */
@Getter
@Setter
@ToString
public class FileContentModel implements Serializable {

    private String fileContent;

    private FileContentType fileContentType;

    private String fileName;

    private FileFormat fileFormat;
}
