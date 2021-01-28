package com.server.bluedotproject.service;

import com.server.bluedotproject.dto.request.CanvasApiRequest;
import com.server.bluedotproject.entity.Artist;
import com.server.bluedotproject.entity.Canvas;
import com.server.bluedotproject.entity.repository.ArtistRepository;
import com.server.bluedotproject.entity.repository.CanvasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CanvasService {

    private CanvasRepository canvasRepository;
    private ArtistRepository artistRepository;

    public String create(CanvasApiRequest canvasApiRequest){
        Optional<Artist> findArtist = artistRepository.findById(canvasApiRequest.getArtistId());

        if(findArtist.isPresent()){
            Artist artist = findArtist.get();

            Canvas canvas = Canvas.builder()
                    .artist(artist)
                    .cashNeedAmount(canvasApiRequest.getCashNeedAmount())
                    .link(canvasApiRequest.getLink())
                    .build();
            canvasRepository.save(canvas);

            return "OK";

        }else{
            return "ERROR";
        }
    }
}
