package com.server.bluedotproject.service;

import com.server.bluedotproject.config.AppProperties;
import com.server.bluedotproject.dto.request.EmailMessageApiRequest;
import com.server.bluedotproject.dto.request.AccountApiRequest;
import com.server.bluedotproject.dto.response.EmailApiResponse;
import com.server.bluedotproject.entity.*;
import com.server.bluedotproject.entity.enumclass.IsDeleted;
import com.server.bluedotproject.entity.enumclass.RoleName;
import com.server.bluedotproject.entity.enumclass.Type;
import com.server.bluedotproject.entity.repository.*;
import com.server.bluedotproject.exceptions.*;
import com.server.bluedotproject.service.mail.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;


@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {



    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserHasRoleRepository userHasRoleRepository;
    private final UserHasGenreRepository userHasGenreRepository;
    private final EmailService emailService;
    private final AppProperties appProperties;
    private final TemplateEngine templateEngine;
    private final GenreRepository genreRepository;
    private final UserRepository userRepository;


    /**
     * email-check
     * 있으면 true , 없으면 false 반환
     */
    public boolean emailIsPresent(String email) {

        Optional<User> findUser = userRepository.findByEmail(email);

        if(findUser.isPresent()){
            return true;
        }else{
            return false;
        }
    }

    /**
     * 회원가입
     */
    public Long create(AccountApiRequest request) {

        // email, nickname 중복 체크
        userRepository.findByEmail(request.getEmail()).ifPresent(user->{throw new DuplicateException(ErrorCode.USER_EMAIL_ALREADY_EXIST);});
        userRepository.findByNickname(request.getNickname()).ifPresent(user -> {throw new DuplicateException(ErrorCode.USER_NICKNAME_ALREADY_EXIST);});


        String encodedPassword = passwordEncoder.encode(request.getPassword());

        User newUser = User.builder()
                .email(request.getEmail())
                .password(encodedPassword)
                .name(request.getName())
                .followingCount(0)
                .isDeleted(IsDeleted.NO)
                .paint(0)
                .nickname(request.getNickname())
                .createdAt(LocalDateTime.now())
                .build();


        //-- 이메일 인증 토큰 생성 (임의로 랜덤 값 생성) --//
        newUser.generateEmailCheckToken();

        User registeredUser = userRepository.save(newUser);

        // 유저 권한 생성
        createRoleCustomer(registeredUser.getId());

        // 유저 취향 생성
        request.getGenreNameList().forEach(item -> {
            Genre genre = genreRepository.findByName(item).orElseThrow(() -> new NotExistException(ErrorCode.GENRE_DOES_NOT_EXIST));

            UserHasGenre userHasGenre = UserHasGenre.builder()
                    .genre(genre)
                    .user(registeredUser)
                    .build();

            userHasGenreRepository.save(userHasGenre);
        });

        return registeredUser.getId();
    }




    /**
     * 해당 email로 'mail/simple-link.html' email 인증 전송
     * @param email
     */
    public EmailApiResponse sendEmailConfirmToken(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email"));

        Context context = new Context();
        context.setVariable("link", "/check-email-token?token="+ user.getEmailCheckToken() +
                "&email=" + user.getEmail());
        context.setVariable("nickname", user.getName());
        context.setVariable("linkName", "이메일 인증하기");
        context.setVariable("message", "블루닷 서비스를 사용하려면 링크를 클릭하세요.");
        context.setVariable("host", appProperties.getHost());

        String message = templateEngine.process("mail/simple-link", context);

        EmailMessageApiRequest emailMessage = EmailMessageApiRequest.builder()
                .to(user.getEmail())
                .subject("블루닷, 이메일 인증")
                .message(message)
                .build();

        emailService.sendEmail(emailMessage);

        return EmailApiResponse.builder()
                .message("send to :" + user.getEmail())
                .build();
    }

    /**
     * 이메잍 토큰 확인
     */
    public EmailApiResponse checkEmailConfirm(String email, String token) {

        User user = userRepository.findByEmail(email).orElseThrow(() -> new NotExistException(ErrorCode.USER_DOES_NOT_EXIST) );

        if(!user.getEmailCheckToken().equals(token)){
            throw new AuthNNotAllowedException(ErrorCode.EMAIL_CONFIRM_NOT_ALLOWED);
        }

        //-- 이메일 인증 완료 user db에 저장 --//
        user.completeEmailConfirm();

        //-- ROLE_CUSTOMER -> ROLE_MEMBER로 변경 --//
        if(!user.isEmailVerified()){
            throw new AuthNNotAllowedException(ErrorCode.ROLE_DOES_NOT_ACCEPTED);
        }
        createRoleMember(user.getId());

        return EmailApiResponse.builder()
                .message("email 인증 완료")
                .build();
    }

    /**
     * 회원가입시 ROLE_CUSTOMER 권한 자동 부여
     */
    private String createRoleCustomer(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(
                        () -> new NotExistException(ErrorCode.USER_DOES_NOT_EXIST)
                );

        Role role = roleRepository.findByRoleName(RoleName.ROLE_CUSTOMER)
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


    /**
     * 이메일 인증 완료시 ROLE_MEMBER 권한 부여
     */
    private String createRoleMember(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(
                        ()-> new NotExistException(ErrorCode.USER_DOES_NOT_EXIST)
                );
        Role role = roleRepository.findByRoleName(RoleName.ROLE_MEMBER)
                .orElseThrow(
                        ()-> new NotExistException(ErrorCode.ROLE_DOES_NOT_EXIST)
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