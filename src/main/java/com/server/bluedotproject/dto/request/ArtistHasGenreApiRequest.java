package com.server.bluedotproject.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ArtistHasGenreApiRequest {

    private Long artistId;

    private List<String> genreList;
}
