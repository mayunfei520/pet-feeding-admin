package com.petfeeding.platform.common.result;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应封装
 *
 * @param <T> 数据泛型
 */
@Data
@NoArgsConstructor
public class R<T> {

    /** 状态码 */
    private int code;
    /** 响应消息 */
    private String message;
    /** 响应数据 */
    private T data;

    private R(int code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    /** 成功 */
    public static <T> R<T> ok() {
        return new R<>(200, "操作成功", null);
    }

    public static <T> R<T> ok(T data) {
        return new R<>(200, "操作成功", data);
    }

    public static <T> R<T> ok(String message, T data) {
        return new R<>(200, message, data);
    }

    /** 失败 */
    public static <T> R<T> fail() {
        return new R<>(500, "操作失败", null);
    }

    public static <T> R<T> fail(String message) {
        return new R<>(500, message, null);
    }

    public static <T> R<T> fail(int code, String message) {
        return new R<>(code, message, null);
    }
}
