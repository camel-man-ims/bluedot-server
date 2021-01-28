package com.server.bluedotproject.entity.repository;

import com.server.bluedotproject.entity.DotVideoComments;
import com.server.bluedotproject.entity.DotVideoCommentsLikes;
import com.server.bluedotproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DotVideoCommentsLikesRepository extends JpaRepository<DotVideoCommentsLikes,Long> {
    Optional<DotVideoCommentsLikes> findByDotVideoCommentsAndUser(DotVideoComments dotVideoComments, User user);

    List<DotVideoCommentsLikes> findByDotVideoComments(DotVideoComments dotVideoComments);
}
