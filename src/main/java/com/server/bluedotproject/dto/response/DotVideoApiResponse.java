package com.server.bluedotproject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DotVideoApiResponse {

    private Long dotVideoId;

    private ArtistApiResponse artist;

    private String videoLink;

    private Integer viewCount;

    private Integer likesCount;

    private Integer commentsCount;
}