package com.server.bluedotproject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class ArtistApiResponse {
    private Long artistId;

    private String name;

    private String nickname;

    private String description;

    private String profileImg;

    private Integer followedCount;

    private Integer averageCanvasTime;
}
