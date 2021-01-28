package com.server.bluedotproject.dto.response;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class GenreApiResponse {

    private Long genreId;

    private String genreName;
}
