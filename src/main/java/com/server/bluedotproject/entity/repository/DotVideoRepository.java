package com.server.bluedotproject.entity.repository;

import com.server.bluedotproject.entity.DotVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DotVideoRepository extends JpaRepository<DotVideo,Long> {
}
