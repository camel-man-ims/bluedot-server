package com.server.bluedotproject.dto.request;

import com.server.bluedotproject.entity.enumclass.AccessRange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PostApiRequest {

    private String title;

    private String description;

    private AccessRange accessRange;
}
