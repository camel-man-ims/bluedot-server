package com.server.bluedotproject.service;


import com.server.bluedotproject.entity.User;
import com.server.bluedotproject.entity.repository.UserRepository;
import com.server.bluedotproject.exceptions.ErrorCode;
import com.server.bluedotproject.exceptions.NotExistException;
import com.server.bluedotproject.exceptions.PasswordWrongException;
import com.server.bluedotproject.security.UserPrincipal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
public class SessionService  implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public SessionService(@Lazy UserRepository userRepository, @Lazy PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 인증/인가를 위해 DB에서 User정보 가져오기
     * @param username -> user.email
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with email")
                );

        log.info(" loadUserByUsername 조회 성공 ");


        return UserPrincipal.create(user);
    }


    /**
     * 인증/인가를 위해 DB에서 User정보 가져오기
     * @param id
     * @return
     */
    @Transactional
    public UserDetails loadUserById(Long id){
        User user = userRepository.findById(id) .orElseThrow(() -> new NotExistException(ErrorCode.USER_DOES_NOT_EXIST));

        log.info(" loadUserById 조회 성공 ");

        return UserPrincipal.create(user);
    }

    /**
     * 인증
     * (email,password) ->  user 정보 제공
     */
    public User authenticate(String email, String password) {

        //이메일 존재하지 않으면 예외처리
        User user = userRepository.findByEmail(email).orElseThrow(() -> new NotExistException(ErrorCode.USER_DOES_NOT_EXIST));

        //패스워드 예외처리
        if(!passwordEncoder.matches(password, user.getPassword())){
            throw new PasswordWrongException(ErrorCode.USER_PASSWORD_WRONG);
        }

        return user;
    }
}
