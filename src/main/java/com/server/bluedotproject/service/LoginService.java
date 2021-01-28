package com.server.bluedotproject.service;


import com.server.bluedotproject.dto.request.LoginApiRequest;
import com.server.bluedotproject.entity.Role;
import com.server.bluedotproject.entity.User;
import com.server.bluedotproject.entity.UserHasRole;
import com.server.bluedotproject.entity.enumclass.RoleName;
import com.server.bluedotproject.entity.repository.RoleRepository;
import com.server.bluedotproject.entity.repository.UserHasRoleRepository;
import com.server.bluedotproject.entity.repository.UserRepository;
import com.server.bluedotproject.exceptions.ErrorCode;
import com.server.bluedotproject.exceptions.ErrorNotFoundException;
import com.server.bluedotproject.exceptions.NotExistException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService{

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private RoleRepository roleRepository;
    private UserHasRoleRepository userHasRoleRepository;

    /**
     * email-check
     */
    public boolean emailIsExisted(String email) {

        Optional<User> existed = userRepository.findByEmail(email);

        if (existed.isPresent()) {
           return true;
        }

        return false;
    }

    /**
     * 회원가입
     */
    public Long create(LoginApiRequest request) {

        String encodedPassword = passwordEncoder.encode(request.getPassword());

        //TODO : 이메일 중복, 닉네임 중복
        User newUser = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .nickname(request.getNickname())
                .createdAt(LocalDateTime.now())
                .build();

        User returnData = userRepository.save(newUser);

        createRoleUser(returnData.getId());


        return returnData.getId();
    }

    /**
     * 회원가입시 ROLE_USER 권한 자동 부여
     */
    private String createRoleUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(
                        () -> new NotExistException(ErrorCode.USER_DOES_NOT_EXIST)
                );

        Role role = roleRepository.findByRoleName(RoleName.ROLE_MEMBER)
                .orElseThrow(
                        () -> new ErrorNotFoundException(ErrorCode.USER_CREATE_FAIL)
                );


        UserHasRole creatRole = UserHasRole.builder()
                                .role(role)
                                .user(user)
                                .build();

        userHasRoleRepository.save(creatRole);

        log.info("creatRole : " + creatRole.getId());

        return creatRole.getRole().toString();
    }
}
