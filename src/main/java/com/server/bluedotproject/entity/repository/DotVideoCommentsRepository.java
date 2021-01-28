package com.server.bluedotproject.entity.repository;

import com.server.bluedotproject.entity.DotVideoComments;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DotVideoCommentsRepository extends JpaRepository<DotVideoComments,Long> {
}
