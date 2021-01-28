package com.server.bluedotproject.entity.repository;

import com.server.bluedotproject.entity.User;
import com.server.bluedotproject.entity.UserHasRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserHasRoleRepository extends JpaRepository<UserHasRole, Long> {
    Optional<UserHasRole> findByUser(User user);
}
