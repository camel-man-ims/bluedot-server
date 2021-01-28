package com.server.bluedotproject.dto.response;

import com.server.bluedotproject.entity.enumclass.AccessRange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class PostApiResponse {

    private Long postId;

    private Long artistId;

    private String link;

    private String thumbnail;

    private String title;

    private String description;

    private LocalDateTime createdAt;

    private AccessRange accessRange;

    private Integer viewCount;

    private Integer likesCount;

    private Integer commentsCount;
}
