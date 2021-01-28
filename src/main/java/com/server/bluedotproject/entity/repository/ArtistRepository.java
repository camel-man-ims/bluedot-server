package com.server.bluedotproject.entity.repository;

import com.server.bluedotproject.entity.Artist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ArtistRepository extends JpaRepository<Artist,Long> {
    Page<Artist> findAll(Pageable pageable);
}
