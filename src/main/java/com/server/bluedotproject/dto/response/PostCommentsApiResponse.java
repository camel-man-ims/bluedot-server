package com.server.bluedotproject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class PostCommentsApiResponse {
    private Long postCommentsId;

    private Long userId;

    private String comments;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    private Integer likesCount;
}
