package com.server.bluedotproject.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountApiRequest {

    private String email;

    private String password;

    private String name;

    private String nickname;

    private List<String> genreNameList;

}
