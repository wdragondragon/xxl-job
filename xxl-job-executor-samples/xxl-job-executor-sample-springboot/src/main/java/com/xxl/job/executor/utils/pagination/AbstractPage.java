package com.xxl.job.executor.utils.pagination;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * @author llk
 * @date 2019-11-09 23:09
 */
@Data
@Accessors(chain = true)
public abstract class AbstractPage<T> {

    private Long total; // 总条数

    private Long pageNum; // 页码

    private Long pageSize; // 页面大小

    private Long pages;  // 总页数

    public Long getPages() {
        if (pages == null) {
            if (total == null || pageSize == null) {
                return null;
            }
            return (total + pageSize - 1) / pageSize;
        }
        return pages;
    }

    @JsonIgnore
    protected abstract T getRecord();
}
