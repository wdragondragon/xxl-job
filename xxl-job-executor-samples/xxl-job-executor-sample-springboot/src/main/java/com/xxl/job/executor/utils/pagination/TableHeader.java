package com.xxl.job.executor.utils.pagination;

import java.lang.annotation.*;


/**
 * @author llk
 * @date 2019-11-09 23:55
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TableHeader {

    String value(); // 字段中文名称

    int width() default 100; // 宽度

    boolean sortable() default false; // 是否可排序

    // TODO: 2019-11-10 分组
    String[] groups() default {}; // 分组

}
