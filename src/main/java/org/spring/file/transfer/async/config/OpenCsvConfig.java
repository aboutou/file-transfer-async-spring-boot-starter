package org.spring.file.transfer.async.config;

import com.opencsv.bean.ColumnPositionMappingStrategy;
import org.spring.file.transfer.async.core.I18nHandler;
import org.spring.file.transfer.async.core.impl.OpenCsvFileConverter;
import org.spring.file.transfer.async.domain.service.opencsv.CsvFilter;
import org.spring.file.transfer.async.domain.service.opencsv.impl.CsvBigDecimalConvertFilterImpl;
import org.spring.file.transfer.async.domain.service.opencsv.impl.CsvNumberFilterImpl;
import org.spring.file.transfer.async.domain.service.opencsv.impl.CsvUnitConvertFilterImpl;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * @author tiny
 * 
 * @since 2023/5/17 下午6:04
 */
@ConditionalOnClass(ColumnPositionMappingStrategy.class)
public class OpenCsvConfig {

    @Bean
    public CsvNumberFilterImpl csvNumberFilter() {
        return new CsvNumberFilterImpl();
    }

    @Bean
    public CsvBigDecimalConvertFilterImpl csvBigDecimalConvertFilter() {
        return new CsvBigDecimalConvertFilterImpl();
    }

    @Bean
    public CsvUnitConvertFilterImpl csvUnitConvertFilter() {
        return new CsvUnitConvertFilterImpl();
    }

    @Bean
    public OpenCsvFileConverter openCsvFileConverter(List<CsvFilter> csvFilters, I18nHandler i18nHandler) {
        return new OpenCsvFileConverter(csvFilters, i18nHandler);
    }

}
