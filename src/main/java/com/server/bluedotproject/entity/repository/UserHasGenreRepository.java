package com.server.bluedotproject.entity.repository;

import com.server.bluedotproject.entity.Genre;
import com.server.bluedotproject.entity.User;
import com.server.bluedotproject.entity.UserHasGenre;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserHasGenreRepository extends JpaRepository<UserHasGenre,Long> {

    Optional<UserHasGenre> findByUserAndGenre(User user, Genre genre);

    List<UserHasGenre> findByUser(User user);
    List<UserHasGenre> findByUser(User user,Pageable pageable);

}
