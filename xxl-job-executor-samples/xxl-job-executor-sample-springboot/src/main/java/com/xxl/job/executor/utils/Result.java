package com.xxl.job.executor.utils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @author llk
 * @date 2019-10-04 19:25
 */
@Data
@NoArgsConstructor
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {

    /**
     * 返回码2
     */
    private int code;

    /**
     * 返回信息
     */
    private String msg;

    /**
     * 返回数据
     */
    private T data;

    /**
     * 出错的请求路径
     */
    private String path;

    @JsonIgnore
    private transient static Object defaultData = new Object();

    /**
     * 时间戳
     */
    private long timestamp = System.currentTimeMillis();

    private Result(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    private Result(int code, String msg, T data, String path) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.path = path;
    }

    @SuppressWarnings("unchecked")
    public static <T> Result<T> success(T data) {
        if (data == null) {
            data = (T) defaultData;
        }
        return new Result<>(200, "success", data);
    }

    public static <T> Result<T> success(String msg) {
        return new Result<>(200, msg, null);
    }


    public static Result<Object> success() {
        return success(defaultData);
    }


    public static <T> Result<T> error(int code, String message, String path) {
        return new Result<>(code, message, null, path);
    }


}
