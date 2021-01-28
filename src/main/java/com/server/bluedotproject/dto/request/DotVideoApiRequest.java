package com.server.bluedotproject.dto.request;

import com.server.bluedotproject.entity.enumclass.AccessRange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DotVideoApiRequest {

    private Long artistId;

    private MultipartFile videoFile;

    private String thumbnail;

    private AccessRange accessRange;
}
