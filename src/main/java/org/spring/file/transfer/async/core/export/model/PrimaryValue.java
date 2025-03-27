package org.spring.file.transfer.async.core.export.model;

import lombok.*;

import java.io.Serializable;

/**
 * @author tiny
 * @apiNote
 * @since 2023/5/14 下午10:20
 */
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PrimaryValue<T extends Serializable> {

    /**
     * 主键ID/唯一索引的最小值
     */
    private T minValue;

    /**
     * 主键ID/唯一索引的最大值
     */
    private T maxValue;

}
