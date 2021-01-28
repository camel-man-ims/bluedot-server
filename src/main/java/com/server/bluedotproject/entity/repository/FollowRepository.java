package com.server.bluedotproject.entity.repository;

import com.server.bluedotproject.entity.Follow;
import com.server.bluedotproject.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow,Long> {
    Optional<Follow> findByFollowingUserAndFollowedUser(User followingUser,User followedUser);

    List<Follow> findByFollowingUser(User followingUser, Pageable pageable);
}
