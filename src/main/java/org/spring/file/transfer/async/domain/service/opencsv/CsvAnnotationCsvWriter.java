package org.spring.file.transfer.async.domain.service.opencsv;

import com.opencsv.CSVWriter;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.util.List;

/**
 * @author bm
 */
public class CsvAnnotationCsvWriter extends CSVWriter {

    private List<Field> fields;
    private int length = 0;
    private List<CsvFilter> csvFilter;

    /**
     * @param writer
     * @param separator  分割符
     * @param quotechar  引用符
     * @param escapechar 转义付
     * @param lineEnd    换行符
     * @param fields     字段
     * @param csvFilter  拦截器
     */
    public CsvAnnotationCsvWriter(Writer writer, char separator, char quotechar, char escapechar, String lineEnd, List<Field> fields, List<CsvFilter> csvFilter) {
        super(writer, separator, quotechar, escapechar, lineEnd);
        this.fields = fields;
        this.csvFilter = csvFilter;
    }

    @Override
    protected void writeNext(String[] nextLine, boolean applyQuotesToAll, Appendable appendable) throws IOException {
        if (nextLine == null) {
            return;
        }
        for (int i = 0; i < nextLine.length; i++) {
            if (i != 0) {
                appendable.append(separator);
            }
            String nextElement = StringUtils.trim(nextLine[i]);
            String sourceElement = nextElement;
            if (StringUtils.isBlank(nextElement)) {
                continue;
            }
            if (length > 0) {
                if (csvFilter != null) {
                    for (CsvFilter filter : csvFilter) {
                        nextElement = filter.handleBefore(sourceElement, nextElement, fields.get(i));
                    }
                }
            }
            // 处理单引号问题
            nextElement = processValue(nextElement);
            // 处理引用符问题
            nextElement = appendQuoteCharacterIfNeeded(applyQuotesToAll, nextElement);
            if (length > 0) {
                if (csvFilter != null) {
                    for (CsvFilter filter : csvFilter) {
                        nextElement = filter.handleAfter(sourceElement, nextElement, fields.get(i));
                    }
                }
            }
            appendable.append(nextElement);
        }
        appendable.append(lineEnd);
        writer.write(appendable.toString());
        length++;
    }

    /**
     * 处理引用符问题
     *
     * @param applyQuotesToAll
     * @param element
     */
    private String appendQuoteCharacterIfNeeded(boolean applyQuotesToAll, String element) {
        if (applyQuotesToAll && quotechar != NO_QUOTE_CHARACTER) {
            return quotechar + element + quotechar;
        }
        return element;
    }

    protected String processValue(String nextStr) {
        if (nextStr == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < nextStr.length(); j++) {
            char nextChar = nextStr.charAt(j);
            if (nextChar == quotechar) {
                sb.append(escapechar);
            }
            sb.append(nextChar);
        }
        return sb.toString();
    }

    public void setLength(int length) {
        this.length = length;
    }
}
