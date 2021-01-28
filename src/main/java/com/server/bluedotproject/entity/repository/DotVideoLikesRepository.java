package com.server.bluedotproject.entity.repository;

import com.server.bluedotproject.entity.DotVideo;
import com.server.bluedotproject.entity.DotVideoLikes;
import com.server.bluedotproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DotVideoLikesRepository extends JpaRepository<DotVideoLikes,Long> {
    Optional<DotVideoLikes> findByDotVideoAndUser(DotVideo dotVideo, User user);
}
