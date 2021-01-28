package com.server.bluedotproject.entity.repository;

import com.server.bluedotproject.entity.Role;
import com.server.bluedotproject.entity.enumclass.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByRoleName(RoleName roleName);

    Optional<Role> findById(Long id);


}
