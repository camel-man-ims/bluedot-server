package com.server.bluedotproject.entity.repository;

import com.server.bluedotproject.entity.Post;
import com.server.bluedotproject.entity.PostComments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostCommentsRepository extends JpaRepository<PostComments,Long> {

    List<PostComments> findByPost(Post post);
}
