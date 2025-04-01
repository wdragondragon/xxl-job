package com.xxl.job.executor.utils.pagination;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @description: 分页查询列表
 * @author: zgs
 * @create: 2021/11/22
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PageList<T> extends AbstractPage<List<T>> {
    private List<T> records;

    @Override
    protected List<T> getRecord() {
        return this.records;
    }
}
