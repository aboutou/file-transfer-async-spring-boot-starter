package org.spring.file.transfer.async.core.export.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author tiny
 * 
 * @since 2023/5/15 上午10:18
 */
@Getter
@Setter
@ToString
public class ExportPageModel<T> {

    private int pageNo;

    private int pageSize;

    private boolean nextPage;

    private T pageParam;

    private int successPageNum = -1;

    public ExportPageModel(T pageParam) {
        this.pageParam = pageParam;
        this.pageNo = 1;
        this.pageSize = 500;
        this.nextPage = false;
        this.successPageNum = -1;
    }

    public boolean hasNextPage() {
        return this.nextPage;
    }

    public void nextPage() {
        this.nextPage = false;
        this.pageNo = this.pageNo + 1;
        this.successPageNum = -1;
    }

}
