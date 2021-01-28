package com.server.bluedotproject.dto.response;

import com.server.bluedotproject.entity.enumclass.AccessRange;
import com.server.bluedotproject.entity.enumclass.ArtistState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class PaymentApiResponse {
    private Long artistId;

    private Long userId;

    private AccessRange accessRange;

    private ArtistState artistState;

    private Integer cashAmountUsed;

    private LocalDateTime createdAt;

    private String requestInfo;

    private String sendEmail;
}
