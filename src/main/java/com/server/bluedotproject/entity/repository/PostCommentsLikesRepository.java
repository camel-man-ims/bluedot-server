package com.server.bluedotproject.entity.repository;

import com.server.bluedotproject.entity.PostComments;
import com.server.bluedotproject.entity.PostCommentsLikes;
import com.server.bluedotproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PostCommentsLikesRepository extends JpaRepository<PostCommentsLikes,Long> {
    Optional<PostCommentsLikes> findByUserAndPostComments(User user, PostComments postComments);
}
