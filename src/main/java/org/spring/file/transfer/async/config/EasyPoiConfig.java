package org.spring.file.transfer.async.config;

import cn.afterturn.easypoi.excel.entity.ImportParams;
import org.spring.file.transfer.async.core.I18nHandler;
import org.spring.file.transfer.async.core.impl.EasyPoiFileConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;

/**
 * @author tiny
 * @apiNote
 * @since 2023/5/17 下午6:02
 */
@ConditionalOnClass(ImportParams.class)
public class EasyPoiConfig {


    @Bean
    public EasyPoiFileConverter easyPoiFileConverter(I18nHandler i18nHandler) {
        EasyPoiFileConverter easyPoiFileConverter = new EasyPoiFileConverter();
        easyPoiFileConverter.setI18nHandler(i18nHandler);
        return easyPoiFileConverter;
    }
}
