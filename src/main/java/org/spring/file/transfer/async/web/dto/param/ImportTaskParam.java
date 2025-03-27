package org.spring.file.transfer.async.web.dto.param;

import org.spring.file.transfer.async.commons.FileContentType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author tiny
 * @apiNote
 * @since 2023/5/12 下午11:24
 */
@Getter
@Setter
@ToString
public class ImportTaskParam {


    /**
     * 文件类型，可以选url, base64
     */
    private FileContentType fileContentType;

    /**
     * 上传文件的内容，可以是链接，可以是base字符串
     */
    private String fileContent;

    /**
     * 文件名称
     */
    private String fileName;

}
