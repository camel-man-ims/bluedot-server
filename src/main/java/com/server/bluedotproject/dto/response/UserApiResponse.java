package com.server.bluedotproject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UserApiResponse {

    private String email;

    private String name;

    private String nickname;

    private Integer followingCount;

    private LocalDateTime createdAt;

}
