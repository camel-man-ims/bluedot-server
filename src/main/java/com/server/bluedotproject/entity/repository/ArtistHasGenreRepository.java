package com.server.bluedotproject.entity.repository;

import com.server.bluedotproject.entity.ArtistHasGenre;
import com.server.bluedotproject.entity.Genre;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtistHasGenreRepository extends JpaRepository<ArtistHasGenre,Long> {
    List<ArtistHasGenre> findByGenre(Genre genre, Pageable pageable);
}
