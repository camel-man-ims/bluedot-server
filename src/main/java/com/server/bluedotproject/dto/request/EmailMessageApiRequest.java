package com.server.bluedotproject.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmailMessageApiRequest {

    private String to;

    private String subject;

    private String message;
}