package com.server.bluedotproject.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtistApiRequest {

    private String description;

    private String img;

    private Integer followedCount;

    private String bannerImg;

    private Integer averageCanvasTime;
}
