package com.xxl.job.executor.utils.pagination;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * @author llk
 * @date 2019-11-09 23:34
 */
@Data
public class Table<E> {

    private List<Header> headers = new ArrayList<>();

    private List<E> bodies = new ArrayList<>();

    void addHeader(Header header) {
        this.headers.add(header);
    }

    public void addHeader(String name, String title) {
        this.addHeader(name, title, 100, false);
    }

    public void addHeader(String name, String title,boolean sortable) {
        this.addHeader(name, title, 100, sortable);
    }

    public void addHeader(String name, String title, Integer width, boolean sortable) {
        Header header = new Header();
        header.setName(name);
        header.setTitle(title);
        header.setWidth(width);
        header.setSortable(sortable);
        this.addHeader(header);
    }

    @Data
    public static class Header {

        private String name; // 表头字段名

        private String title; // 表头字段中文名

        private Integer width = 100; // 宽度

        private boolean sortable; // 可排序

    }

}
