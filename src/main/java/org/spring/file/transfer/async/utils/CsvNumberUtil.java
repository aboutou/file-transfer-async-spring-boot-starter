package org.spring.file.transfer.async.utils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.util.regex.Pattern;

/**
 * @author bm
 */
public abstract class CsvNumberUtil {


    public static Pattern pattern = Pattern.compile("^-?\\d*\\.?\\d{1,}$");

    public static boolean isExcelNumber(String sourceElement) {
        if (StringUtils.isBlank(sourceElement)) {
            return false;
        }
        // 1、检查字符串中是否只含有数字，负数和小数都会返回 false
        // 2、str 为 null 或者为空 返回 false
        // 3、底层调用 {@link StringUtils#isNumeric(java.lang.CharSequence)}
        if (NumberUtils.isDigits(sourceElement)
                && (sourceElement.length() > 10 || StringUtils.startsWith(sourceElement, "0"))) {
            return true;
        } else if (NumberUtils.isParsable(sourceElement)
                && StringUtils.length(sourceElement) > 10) {
            // 1.检查字符串是否可以解析为数字，即 {@link Integer#parseInt(String)},{@link Long#parseLong(String)}, {@link Float#parseFloat(String),{@link Double#parseDouble(String)}.
            // 这个方法可以防止调用上面的方法时出现  {@link java.text.ParseException}
            // 注意只支持 10 进制，支持正负数，不支持 8进制、16进制、不支持科学计数法，也不支持类型限定符（如 3000L，3.14F）
            return true;
        } else if (!pattern.matcher(sourceElement).matches() && NumberUtils.isCreatable(sourceElement)) {
            // 1、判断字符串是否为有效的 java 数字，支持16进制、8进制、10进制、正数负数、科学计数法（如8.788006e+05）、类型限定符（110L、3.14f）
            // 2、0X 开头当做 16 进制处理，如 0X89F9；以0开头的非十六进制字符串作为八进制值处理，如 076、-076等
            // 3、注意例如 098 不是八进制，因为8进制是0-7，没有8、9，所以会当做10进制处理，而此时不是数字，所以为false.
            // 4、str 为空或者为 null，都返回 false
            return true;
        } else if (StringUtils.containsIgnoreCase(sourceElement, "E")
                && NumberUtils.isCreatable(removeStart(sourceElement))) {
            // 当科学计数法里面是0开头的，NumberUtils.isCreatable判断会有问题
            return true;
        } else if (StringUtils.startsWithIgnoreCase(sourceElement, "%") && NumberUtils.isCreatable(removeStart(StringUtils.removeEndIgnoreCase(sourceElement, "%")))) {
            return true;
        } else if (StringUtils.endsWithIgnoreCase(sourceElement, "%") && NumberUtils.isCreatable(removeStart(StringUtils.removeEndIgnoreCase(sourceElement, "%")))) {
            return true;
        }
        return false;
    }

    public static String removeStart(String str) {
        String s = StringUtils.removeStart(str, "0");
        if (StringUtils.startsWith(s, "0")) {
            return removeStart(s);
        }
        return s;
    }

    public static String getCsvData(String data) {
        StringBuilder sb = new StringBuilder();
        String dataStr = StringUtils.replaceChars(String.valueOf(data), "\"", "\"\"");
        sb.append("\"");
        sb.append(dataStr);
        sb.append("\"");
        String s = sb.toString();
        boolean number = CsvNumberUtil.isExcelNumber(StringUtils.trim(data));
        if (number) {
            s = "=" + s;
        }
        return s;
    }
}
