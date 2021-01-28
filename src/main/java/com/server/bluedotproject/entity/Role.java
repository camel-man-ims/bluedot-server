package com.server.bluedotproject.entity;

import com.server.bluedotproject.entity.enumclass.RoleName;
import lombok.Getter;

import javax.persistence.*;

@Entity
@Getter
@Table(name = "role")
public class Role {

    @Id @Column(name = "role_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private RoleName roleName;

}
