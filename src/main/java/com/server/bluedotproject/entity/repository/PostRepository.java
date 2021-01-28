package com.server.bluedotproject.entity.repository;

import com.server.bluedotproject.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post,Long> {

    List<Post> findByTitleContaining(String keyword, Pageable pageable);

    List<Post> findByTitleContaining(String keyword);

    List<Post> findByDescriptionContaining(String keyword, Pageable pageable);

    List<Post> findByDescriptionContaining(String keyword);

    Page<Post> findAll(Pageable pageable);

}
