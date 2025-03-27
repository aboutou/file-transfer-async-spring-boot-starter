package org.spring.file.transfer.async.commons;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import java.nio.charset.StandardCharsets;

/**
 * 文件格式
 *
 * @author bm
 */
@Getter
@AllArgsConstructor
public enum FileFormat {


    PDF("PDF", MediaType.APPLICATION_PDF),
    EXCEL_03("XLS", new MediaType("application", "vnd.ms-excel", StandardCharsets.UTF_8)),
    EXCEL_07("XLSX", new MediaType("application", "vnd.openxmlformats-officedocument.spreadsheetml.sheet", StandardCharsets.UTF_8)),
    CSV("CSV", new MediaType("text", "csv", StandardCharsets.UTF_8)),
    ;

    @JsonValue
    private String fileExtensionName;
    private MediaType mediaType;

    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static FileFormat fileFormat(String fileExtensionName) {
        for (FileFormat value : values()) {
            if (StringUtils.equalsIgnoreCase(value.fileExtensionName, fileExtensionName)) {
                return value;
            }
        }
        return null;
    }
}
