package com.server.bluedotproject.dto.request;


import lombok.Data;

/**
 * 로그인 요청 데이터
 */

@Data
public class SessionApiRequest {

    private String email;

    private String password;
}
