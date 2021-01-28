package com.server.bluedotproject.service;

import com.server.bluedotproject.dto.request.UserApiRequest;
import com.server.bluedotproject.entity.*;
import com.server.bluedotproject.entity.enumclass.IsDeleted;
import com.server.bluedotproject.entity.enumclass.RoleName;
import com.server.bluedotproject.dto.request.UserPasswordApiRequest;
import com.server.bluedotproject.entity.*;
import com.server.bluedotproject.entity.repository.*;
import com.server.bluedotproject.exceptions.DuplicateException;
import com.server.bluedotproject.exceptions.ErrorCode;
import com.server.bluedotproject.exceptions.NotExistException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final RoleRepository roleRepository;
    private final UserHasRoleRepository userHasRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final GenreRepository genreRepository;
    private final UserRepository userRepository;
    private final UserHasGenreRepository userHasGenreRepository;
    private final PaymentRepository paymentRepository;

    @Transactional(rollbackFor = {NotExistException.class})
    public User createUser(UserApiRequest userApiRequest) {

        // email, nickname 중복 체크
        userRepository.findByEmail(userApiRequest.getEmail()).ifPresent(user->{throw new DuplicateException(ErrorCode.USER_EMAIL_ALREADY_EXIST);});
        userRepository.findByNickname(userApiRequest.getNickname()).ifPresent(user -> {throw new DuplicateException(ErrorCode.USER_NICKNAME_ALREADY_EXIST);});

        String encodedPassword = passwordEncoder.encode(userApiRequest.getPassword());

        User newUser = User.builder()
                .email(userApiRequest.getEmail())
                .password(encodedPassword)
                .name(userApiRequest.getName())
                .followingCount(0)
                .isDeleted(IsDeleted.NO)
                .paint(100000)
                .nickname(userApiRequest.getNickname())
                .createdAt(LocalDateTime.now())
                .build();

        newUser.generateEmailCheckToken();

        User registeredUser = userRepository.save(newUser);

        // 유저 취향 생성
        userApiRequest.getGenreNameList().forEach(item -> {
            Genre genre = genreRepository.findByName(item).orElseThrow(() -> new NotExistException(ErrorCode.GENRE_DOES_NOT_EXIST));

            UserHasGenre userHasGenre = UserHasGenre.builder()
                    .genre(genre)
                    .user(registeredUser)
                    .build();

            userHasGenreRepository.save(userHasGenre);
        });

        Role role = roleRepository.findByRoleName(RoleName.ROLE_MEMBER).orElseThrow(()->new NotExistException(ErrorCode.USER_MAKES_ROLE_ERROR));

        UserHasRole creatRole = UserHasRole.builder()
                .role(role)
                .user(registeredUser)
                .build();

        userHasRoleRepository.save(creatRole);

        return registeredUser;
    }

    // 유저 취향 반환
    public List<UserHasGenre> getGenreListOfUser(Long userId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new NotExistException(ErrorCode.USER_DOES_NOT_EXIST));

        return userHasGenreRepository.findByUser(user);

    }

    @Transactional(rollbackFor = {NotExistException.class})
    public List<UserHasGenre> updateUserGenre(Long userId, List<String> genreNameList){

        User user = userRepository.findById(userId).orElseThrow(()->new NotExistException(ErrorCode.USER_DOES_NOT_EXIST));

        List<UserHasGenre> userHasGenreList = userHasGenreRepository.findByUser(user);

        userHasGenreList.forEach(userHasGenreRepository::delete);

        genreNameList.forEach(item->{
            Genre findGenre = genreRepository.findByName(item).orElseThrow(()->new NotExistException(ErrorCode.GENRE_DOES_NOT_EXIST));
            UserHasGenre userHasGenre = UserHasGenre.builder()
                    .user(user)
                    .genre(findGenre)
                    .build();
            userHasGenreRepository.save(userHasGenre);
        });

        List<UserHasGenre> insertedUserHasGenreList = userHasGenreRepository.findByUser(user);

        return insertedUserHasGenreList;
    }

    public User findUser(Long id){
        return userRepository.findById(id).orElseThrow(()->new NotExistException(ErrorCode.USER_DOES_NOT_EXIST));
    }

    public Role getUserRole(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NotExistException(ErrorCode.USER_DOES_NOT_EXIST));
        UserHasRole userRole = userHasRoleRepository.findByUser(user).orElseThrow(() -> new NotExistException(ErrorCode.USER_HAS_ROLE_DOES_NOT_EXIST));
        return userRole.getRole();
    }

    public User updateUserPassword(String updatedPassword,Long userId){
        User user = userRepository.findById(userId).orElseThrow(()->new NotExistException(ErrorCode.USER_DOES_NOT_EXIST));

        String encodedPassword = passwordEncoder.encode(updatedPassword);

        User newUser = User.builder()
                .userHasGenreList(user.getUserHasGenreList())
                .createdAt(user.getCreatedAt())
                .password(encodedPassword)
                .dotVideoCommentsList(user.getDotVideoCommentsList())
                .dotVideoLikesList(user.getDotVideoLikesList())
                .email(user.getEmail())
                .followedList(user.getFollowedList())
                .followingCount(user.getFollowingCount())
                .followingList(user.getFollowingList())
                .id(user.getId())
                .isDeleted(user.getIsDeleted())
                .name(user.getName())
                .nickname(user.getNickname())
                .paint(user.getPaint())
                .paymentList(user.getPaymentList())
                .postCommentsLikesList(user.getPostCommentsLikesList())
                .updatedAt(user.getUpdatedAt())
                .postCommentsList(user.getPostCommentsList())
                .postLikesList(user.getPostLikesList())
                .build();

        return userRepository.save(newUser);
    }

    public User getUserByArtist(Artist artist) {
        return userRepository.findById(artist.getId()).orElseThrow(()->new NotExistException(ErrorCode.USER_DOES_NOT_EXIST));
    }
    public Integer getPaintCount(Long userId) {
        return userRepository.findById(userId).orElseThrow(()->new NotExistException(ErrorCode.USER_DOES_NOT_EXIST)).getPaint();
    }
    public List<Payment> getPaymentListOfUser(Long userId) {
        return paymentRepository.findByUser(userId);
    }
    public User getUser(Long userId) {
        return userRepository.findById(userId).orElseThrow(()->new NotExistException(ErrorCode.USER_DOES_NOT_EXIST));
    }

    // <--> Method <--> //

    // 있으면 true , 없으면 false 반환
    public boolean emailIsPresent(String email) {

        Optional<User> findUser = userRepository.findByEmail(email);

        if(findUser.isPresent()){
            return true;
        }else{
            return false;
        }
    }

}
