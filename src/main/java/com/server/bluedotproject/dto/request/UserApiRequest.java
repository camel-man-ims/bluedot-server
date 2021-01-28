package com.server.bluedotproject.dto.request;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserApiRequest {

    private String email;

    private String password;

    private String name;

    private String nickname;

    private List<String> genreNameList;

}