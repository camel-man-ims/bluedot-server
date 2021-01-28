package com.server.bluedotproject.dto.request;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class GenreApiRequest {

    private List<String> GenreNameList;

    private Long userId;
}
