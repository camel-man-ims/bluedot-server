package com.server.bluedotproject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Data
public class DotVideoApiResponseS3<B,T> {
    public static DotVideoApiResponseS3<Integer,String> OK = new DotVideoApiResponseS3<>(200, "", "OK");

    private int code;
    private String message;
    private T data;

    public static <B,T> DotVideoApiResponseS3<B,T> of(T data) {
        return new DotVideoApiResponseS3<>(200, "upload 성공", data);
    }
}
