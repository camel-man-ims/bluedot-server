package com.server.bluedotproject.service;

import com.server.bluedotproject.entity.Genre;
import com.server.bluedotproject.entity.repository.GenreRepository;
import com.server.bluedotproject.entity.repository.UserHasGenreRepository;
import com.server.bluedotproject.entity.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GenreService {

    private final GenreRepository genreRepository;
    private final UserRepository userRepository;
    private final UserHasGenreRepository userHasGenreRepository;

    public String create(String name){
        Genre genre = Genre.builder()
                .name(name)
                .build();

        genreRepository.save(genre);

        return "OK";
    }

}
