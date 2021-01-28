package com.server.bluedotproject.dto.request;

import lombok.Data;

@Data
public class EmailConfirmApiRequest {

    private String email;
    private String token;
}
