package org.spring.file.transfer.async.utils;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommonResult<T> {

    private Long code;
    private String message;
    private T data;


    public static <T> CommonResult<T> success(T data) {
        CommonResult<T> result = new CommonResult<T>();
        result.code = 200L;
        result.message = "操作成功";
        result.data = data;
        return result;
    }
}
