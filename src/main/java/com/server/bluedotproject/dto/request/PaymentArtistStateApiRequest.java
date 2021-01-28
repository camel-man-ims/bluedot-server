package com.server.bluedotproject.dto.request;

import com.server.bluedotproject.entity.enumclass.ArtistState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class PaymentArtistStateApiRequest {
    private ArtistState artistState;
}
