package org.spring.file.transfer.async.commons;

import lombok.AllArgsConstructor;

import java.util.List;

/**
 * @author tiny
 * 
 * @since 2023/5/6 下午9:58
 */
public interface BizType {

    /**
     * 业务代码
     *
     */
    String getCode();

    /**
     * 业务名称
     *
     */
    String getName();

    /**
     * 错误排序循序
     *
     */
    default List<String> errFieldOrder() {
        return null;
    }

    /**
     * 忽略字段不显示
     *
     */
    default List<String> ignoreField() {
        return null;
    }


    @AllArgsConstructor
    class DefaultBizType implements BizType {

        public DefaultBizType(String code) {
            this.code = code;
            this.name = code;
            this.errFieldOrderList = null;
            this.ignoreFieldList = null;
        }

        public DefaultBizType(BizType bizType) {
            this.code = bizType.getCode();
            this.name = bizType.getName();
            this.errFieldOrderList = bizType.errFieldOrder();
            this.ignoreFieldList = bizType.ignoreField();
        }

        private final String code;
        private final String name;
        private final List<String> errFieldOrderList;
        private final List<String> ignoreFieldList;

        @Override
        public String getCode() {
            return code;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public List<String> errFieldOrder() {
            return errFieldOrderList;
        }

        @Override
        public List<String> ignoreField() {
            return ignoreFieldList;
        }
    }
}
