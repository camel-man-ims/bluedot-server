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
public class DotVideoCommentsApiResponse {
    private Long dotVideoCommentsId;

    private Long dotVideoId;

    private String comments;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

}
