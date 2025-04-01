package com.xxl.job.executor.utils.pagination;

import cn.hutool.core.util.ClassUtil;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author llk
 * @date 2019-11-10 00:09
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PageFactory {

    public static <T> PageTable<T> buildPageTable(long total, long pageNum, long pageSize, TableRef<T> tableRef) {
        return buildPageTable(total, pageNum, pageSize, buildTable(tableRef));
    }


    public static <T> PageTable<T> buildPageTable(long total, long pageNum, long pageSize, Table<T> table) {
        PageTable<T> pageTable = new PageTable<>();
        pageTable.setTotal(total);
        pageTable.setPageNum(pageNum);
        pageTable.setPageSize(pageSize);
        pageTable.setTable(table);
        return pageTable;
    }

    public static <T> Table<T> buildTable(TableRef<T> tableRef) {

        Table<T> table = new Table<>();

        List<T> list = tableRef.getList();
        if (list == null) {
            list = new ArrayList<>();
        }
        Type type = tableRef.getType();
        Field[] fields = ClassUtil.getDeclaredFields((Class<?>) type);
        for (Field field : fields) {
            TableHeader tableHeader = field.getAnnotation(TableHeader.class);
            if (null != tableHeader) {
                Table.Header header = new Table.Header();
                header.setName(field.getName());
                header.setTitle(tableHeader.value());
                header.setWidth(tableHeader.width());
                header.setSortable(tableHeader.sortable());
                table.addHeader(header);
            }
        }
        table.setBodies(list);
        return table;
    }

}
