package com.server.bluedotproject.entity.repository;

import com.server.bluedotproject.entity.Canvas;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CanvasRepository extends JpaRepository<Canvas,Long> {
}
