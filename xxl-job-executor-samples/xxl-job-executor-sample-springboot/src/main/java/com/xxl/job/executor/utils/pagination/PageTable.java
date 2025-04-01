package com.xxl.job.executor.utils.pagination;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author llk
 * @date 2019-11-09 23:04
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class PageTable<E> extends AbstractPage<Table<E>> {

    private Table<E> table;

    @Override
    protected Table<E> getRecord() {
        return this.table;
    }

}
