package com.server.bluedotproject.service;

import com.server.bluedotproject.entity.Artist;
import com.server.bluedotproject.entity.Follow;
import com.server.bluedotproject.entity.User;
import com.server.bluedotproject.entity.repository.ArtistRepository;
import com.server.bluedotproject.entity.repository.FollowRepository;
import com.server.bluedotproject.entity.repository.UserRepository;
import com.server.bluedotproject.exceptions.DuplicateException;
import com.server.bluedotproject.exceptions.ErrorCode;
import com.server.bluedotproject.exceptions.NotExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;

    @Transactional(rollbackFor = {NotExistException.class,DuplicateException.class})
    public Follow createFollow(Long followedId,Long userId) {

        User followingUser = userRepository.findById(userId).orElseThrow(() -> new NotExistException(ErrorCode.FOLLOWING_USER_DOES_NOT_EXIST));
        User followedUser = userRepository.findById(followedId).orElseThrow(() -> new NotExistException(ErrorCode.FOLLOWED_USER_DOES_NOT_EXIST));

        followRepository.findByFollowingUserAndFollowedUser(followingUser, followedUser).ifPresent(follow -> {throw new DuplicateException(ErrorCode.DUPLICATE_FOLLOWING_RELATION);});

        Follow follow = Follow.builder()
                .followedUser(followedUser)
                .followingUser(followingUser)
                .build();

        // followed 카운트 갱신
        updateFollowedUserCount(followedUser,1);

        return followRepository.save(follow);
    }

    @Transactional(rollbackFor = {NotExistException.class})
    public Long deleteFollowUsingFollowingIdAndFollowedId(Long followingId, Long followedId) {

        User followingUser = userRepository.findById(followingId).orElseThrow(()->new NotExistException(ErrorCode.FOLLOWING_USER_DOES_NOT_EXIST));
        User followedUser = userRepository.findById(followedId).orElseThrow(()->new NotExistException(ErrorCode.FOLLOWED_USER_DOES_NOT_EXIST));

        Follow follow = followRepository.findByFollowingUserAndFollowedUser(followingUser, followedUser).orElseThrow(()->new NotExistException(ErrorCode.NOT_FOLLOWING_RELATION));

        followRepository.delete(follow);
      
        updateFollowedUserCount(followedUser,-1);

        return follow.getId();
    }


    public List<Follow> findFollowingListOfUser(Long id, Pageable pageable) {

        User user = userRepository.findById(id).orElseThrow(()->new NotExistException(ErrorCode.USER_DOES_NOT_EXIST));

        return followRepository.findByFollowingUser(user,pageable);
    }


    // <-------> method <-------> //

    private void updateFollowedUserCount(User followedUser, Integer count){

        Artist artist = artistRepository.findById(followedUser.getId()).orElseThrow(()->new NotExistException(ErrorCode.THIS_YOUR_IS_NOT_A_ARTIST));

        Artist updatedArtist = Artist.builder()
                .id(artist.getId())
                .artistHasGenreList(artist.getArtistHasGenreList())
                .averageCanvasTime(artist.getAverageCanvasTime())
                .bannerImg(artist.getBannerImg())
                .canvasList(artist.getCanvasList())
                .description(artist.getDescription())
                .dotVideoList(artist.getDotVideoList())
                .followedCount(artist.getFollowedCount()+count) // 수정 부분
                .profileImg(artist.getProfileImg())
                .postList(artist.getPostList())
                .user(artist.getUser())
                .build();
        artistRepository.save(updatedArtist);
    }

    private void updateFollowingUserCount(User followingUser, Integer count){

        User findUser = userRepository.findById(followingUser.getId()).orElseThrow(()->new NotExistException(ErrorCode.USER_DOES_NOT_EXIST));

        User updatedUser = User.builder()
                .id(findUser.getId())
                .updatedAt(findUser.getUpdatedAt())
                .userHasGenreList(findUser.getUserHasGenreList())
                .createdAt(findUser.getCreatedAt())
                .dotVideoCommentsList(findUser.getDotVideoCommentsList())
                .dotVideoLikesList(findUser.getDotVideoLikesList())
                .email(findUser.getEmail())
                .followedList(findUser.getFollowedList())
                .followingCount(findUser.getFollowingCount()+count)
                .followingList(findUser.getFollowingList())
                .isDeleted(findUser.getIsDeleted())
                .name(findUser.getName())
                .nickname(findUser.getNickname())
                .paint(findUser.getPaint())
                .password(findUser.getPassword())
                .paymentList(findUser.getPaymentList())
                .postCommentsLikesList(findUser.getPostCommentsLikesList())
                .postCommentsList(findUser.getPostCommentsList())
                .postLikesList(findUser.getPostLikesList())
                .build();

        userRepository.save(updatedUser);
    }
}
