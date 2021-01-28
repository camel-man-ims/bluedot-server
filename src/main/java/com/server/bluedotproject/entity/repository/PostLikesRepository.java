package com.server.bluedotproject.entity.repository;

import com.server.bluedotproject.entity.PostLikes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostLikesRepository extends JpaRepository<PostLikes,Long> {
}
