package com.server.bluedotproject.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class DotVideoCommentsPlusCountApi {
    DotVideoCommentsApiResponse dotVideoComments;

    Integer dotVideoCommentsCount;
}
