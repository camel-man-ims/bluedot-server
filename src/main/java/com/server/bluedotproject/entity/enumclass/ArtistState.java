package com.server.bluedotproject.entity.enumclass;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ArtistState {
    ORDER,
    MAKING,
    SHIPPING,
    DONE,
    CANCEL
}
