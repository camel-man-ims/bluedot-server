package com.server.bluedotproject.dto.response;


import lombok.Builder;
import lombok.Data;


/**
 * 로그인 응답 데이터
 */

@Data
@Builder
public class SessionApiResponse {

    private String name;
    private String accessToken;
}
