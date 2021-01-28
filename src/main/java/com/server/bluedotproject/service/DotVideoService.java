package com.server.bluedotproject.service;

import com.server.bluedotproject.entity.*;
import com.server.bluedotproject.entity.enumclass.AccessRange;
import com.server.bluedotproject.entity.repository.*;
import com.server.bluedotproject.exceptions.AuthorizationException;
import com.server.bluedotproject.exceptions.DuplicateException;
import com.server.bluedotproject.exceptions.ErrorCode;
import com.server.bluedotproject.exceptions.NotExistException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DotVideoService {

    private final DotVideoRepository dotVideoRepository;
    private final ArtistRepository artistRepository;
    private final UserRepository userRepository;
    private final DotVideoCommentsRepository dotVideoCommentsRepository;
    private final DotVideoCommentsLikesRepository dotVideoCommentsLikesRepository;
    private final DotVideoLikesRepository dotVideoLikesRepository;

    @Transactional(rollbackFor = {NotExistException.class})
    public DotVideo createDotVideo(String videoUrl,String videoUrl1080,String videoUrl720,String thumbnailUrl, Long artistId, AccessRange accessRange,String description){

        Artist artist = artistRepository.findById(artistId).orElseThrow(()->new NotExistException(ErrorCode.ARTIST_DOES_NOT_EXIST));

                DotVideo dotVideo = DotVideo.builder()
                        .artist(artist)
                        .link(videoUrl)
                        .link720(videoUrl720)
                        .link1080(videoUrl1080)
                        .thumbnail(thumbnailUrl)
                        .description(description)
                        .accessRange(accessRange)
                        .build();

                return dotVideoRepository.save(dotVideo);
    }
    public DotVideoComments createDotVideoComments(Long dotVideoId,Long userId,String comments){
        DotVideo dotVideo = dotVideoRepository.findById(dotVideoId).orElseThrow(()->new NotExistException(ErrorCode.DOT_VIDEO_DOES_NOT_EXIST));
        User user = userRepository.findById(userId).orElseThrow(()->new NotExistException(ErrorCode.USER_DOES_NOT_EXIST));

        DotVideoComments dotVideoComments = DotVideoComments.builder()
                .user(user)
                .dotVideo(dotVideo)
                .likesCount(0)
                .comments(comments)
                .createdAt(LocalDateTime.now())
                .build();

        updateDotVideoElementCommentsCount(dotVideo,1);

        return dotVideoCommentsRepository.save(dotVideoComments);
    }

    public DotVideoCommentsLikes createDotVideoCommentsLikes(Long dotVideoCommentsId,Long userId){
        DotVideoComments dotVideoComments = dotVideoCommentsRepository.findById(dotVideoCommentsId).orElseThrow(()->new NotExistException(ErrorCode.DOT_VIDEO_COMMENTS_DOES_NOT_EXIST));
        User user = userRepository.findById(userId).orElseThrow(()->new NotExistException(ErrorCode.USER_DOES_NOT_EXIST));

        dotVideoCommentsLikesRepository.findByDotVideoCommentsAndUser(dotVideoComments,user).ifPresent(item->{throw new NotExistException(ErrorCode.DUPLICATE_POST_LIKES);});

        DotVideoCommentsLikes dotVideoCommentsLikes = DotVideoCommentsLikes.builder()
                .dotVideoComments(dotVideoComments)
                .user(user)
                .build();

        updateDotVideoCommentsElementLikesCount(dotVideoCommentsLikes.getDotVideoComments(),1);

        return dotVideoCommentsLikesRepository.save(dotVideoCommentsLikes);
    }

    public Integer getDotVideoCommentsElementLikesCount(Long dotVideoCommentsId){
        return dotVideoCommentsRepository.findById(dotVideoCommentsId).orElseThrow(()->new NotExistException(ErrorCode.DOT_VIDEO_COMMENTS_DOES_NOT_EXIST)).getLikesCount();
    }

    public DotVideoLikes createDotVideoLikes(Long dotVideoId, Long userId) {
        DotVideo dotVideo = dotVideoRepository.findById(dotVideoId).orElseThrow(()->new NotExistException(ErrorCode.DOT_VIDEO_DOES_NOT_EXIST));
        User user = userRepository.findById(userId).orElseThrow(()->new NotExistException(ErrorCode.USER_DOES_NOT_EXIST));

        dotVideoLikesRepository.findByDotVideoAndUser(dotVideo,user).ifPresent(dotVideoLikes->{throw new DuplicateException(ErrorCode.DUPLICATE_POST_LIKES);});

        DotVideoLikes dotVideoLikes = DotVideoLikes.builder()
                .dotVideo(dotVideo)
                .user(user)
                .build();

        updateDotVideoElementLikesCount(dotVideo,1);
        return dotVideoLikesRepository.save(dotVideoLikes);

    }

    public Integer getDotVideoLikesCount(Long dotVideoId) {
        return dotVideoRepository.findById(dotVideoId).orElseThrow(()->new NotExistException(ErrorCode.DOT_VIDEO_DOES_NOT_EXIST)).getLikesCount();
    }

    public void deleteDotVideoLikes(Long dotVideoId, Long userId) {
        DotVideo dotVideo = dotVideoRepository.findById(dotVideoId).orElseThrow(()->new NotExistException(ErrorCode.DOT_VIDEO_DOES_NOT_EXIST));
        User user = userRepository.findById(userId).orElseThrow(()->new NotExistException(ErrorCode.USER_DOES_NOT_EXIST));

        DotVideoLikes dotVideoLikes = dotVideoLikesRepository.findByDotVideoAndUser(dotVideo, user).orElseThrow(() -> new NotExistException(ErrorCode.DOT_VIDEO_LIKES_DOES_NOT_EXIST));

        updateDotVideoElementLikesCount(dotVideo,-1);
        dotVideoLikesRepository.delete(dotVideoLikes);
    }

    public void deleteDotVideoCommentsLikes(Long dotVideoCommentsId, Long userId) {
        DotVideoComments dotVideoComments = dotVideoCommentsRepository.findById(dotVideoCommentsId).orElseThrow(()->new NotExistException(ErrorCode.DOT_VIDEO_COMMENTS_DOES_NOT_EXIST));
        User user = userRepository.findById(userId).orElseThrow(()->new NotExistException(ErrorCode.USER_DOES_NOT_EXIST));

        DotVideoCommentsLikes dotVideoCommentsLikes = dotVideoCommentsLikesRepository.findByDotVideoCommentsAndUser(dotVideoComments,user).orElseThrow(()->new NotExistException(ErrorCode.DOT_VIDEO_COMMENTS_DOES_NOT_EXIST));
        updateDotVideoCommentsElementLikesCount(dotVideoComments,-1);
        dotVideoCommentsLikesRepository.delete(dotVideoCommentsLikes);
    }
    public void deleteDotVideoComments(Long dotVideoCommentsId, Long userId) {
        DotVideoComments dotVideoComments = dotVideoCommentsRepository.findById(dotVideoCommentsId).orElseThrow(() -> new NotExistException(ErrorCode.DOT_VIDEO_COMMENTS_DOES_NOT_EXIST));

        if(!dotVideoComments.getUser().getId().equals(userId)){
            throw new AuthorizationException(ErrorCode.AUTHORIZATION_ERROR);
        }

        List<DotVideoCommentsLikes> dotVideoCommentsLikesList = dotVideoCommentsLikesRepository.findByDotVideoComments(dotVideoComments);
        dotVideoCommentsLikesList.forEach(dotVideoCommentsLikesRepository::delete);

        updateDotVideoElementCommentsCount(dotVideoComments.getDotVideo(),-1);

        dotVideoCommentsRepository.delete(dotVideoComments);
    }


    public Integer getDotVideoElementCommentsCount(Long dotVideoId) {
        return dotVideoRepository.findById(dotVideoId).orElseThrow(()->new NotExistException(ErrorCode.DOT_VIDEO_DOES_NOT_EXIST)).getCommentsCount();
    }

    public DotVideo getDotVideoByDotVideoCommentsId(Long dotVideoCommentsId) {
        return dotVideoCommentsRepository.findById(dotVideoCommentsId).orElseThrow(()->new NotExistException(ErrorCode.DOT_VIDEO_COMMENTS_DOES_NOT_EXIST)).getDotVideo();
    }

    public DotVideoComments updateDotVideoComments(Long dotVideoCommentsId, Long userId, String comments) {
        DotVideoComments findDotVideoComments = dotVideoCommentsRepository.findById(dotVideoCommentsId).orElseThrow(()->new NotExistException(ErrorCode.DOT_VIDEO_COMMENTS_DOES_NOT_EXIST));

        if(!findDotVideoComments.getUser().getId().equals(userId)){
            throw new AuthorizationException(ErrorCode.AUTHORIZATION_ERROR);
        }

        DotVideoComments dotVideoComments = DotVideoComments.builder()
                .comments(comments)
                .dotVideo(findDotVideoComments.getDotVideo())
                .likesCount(findDotVideoComments.getLikesCount())
                .createdAt(findDotVideoComments.getCreatedAt())
                .id(findDotVideoComments.getId())
                .createdAt(findDotVideoComments.getCreatedAt())
                .updatedAt(findDotVideoComments.getUpdatedAt())
                .user(findDotVideoComments.getUser())
                .likesCount(findDotVideoComments.getLikesCount())
                .build();
        return dotVideoCommentsRepository.save(dotVideoComments);
    }

    public List<DotVideo> getAllDotVideo() {
        return dotVideoRepository.findAll();
    }

    public DotVideo getDotVideoById(Long dotVideoId) {
        return dotVideoRepository.findById(dotVideoId).orElseThrow(()->new NotExistException(ErrorCode.DOT_VIDEO_DOES_NOT_EXIST));
    }


    // <---> private method <---> //

    private void updateDotVideoCommentsElementLikesCount(DotVideoComments dotVideoComments,int count){
        DotVideoComments updatedDotVideoComments = DotVideoComments.builder()
                .id(dotVideoComments.getId())
                .comments(dotVideoComments.getComments())
                .dotVideo(dotVideoComments.getDotVideo())
                .updatedAt(dotVideoComments.getUpdatedAt())
                .createdAt(dotVideoComments.getCreatedAt())
                .user(dotVideoComments.getUser())
                .likesCount(dotVideoComments.getLikesCount()+count)
                .build();
        dotVideoCommentsRepository.save(updatedDotVideoComments);
    }
    private void updateDotVideoElementLikesCount(DotVideo dotVideo,int count){
        DotVideo updatedDotVideo = DotVideo.builder()
                .id(dotVideo.getId())
                .likesCount(dotVideo.getLikesCount()+count) // <--- 이 부분 수정
                .accessRange(dotVideo.getAccessRange())
                .link(dotVideo.getLink())
                .link720(dotVideo.getLink720())
                .link1080(dotVideo.getLink1080())
                .thumbnail(dotVideo.getThumbnail())
                .artist(dotVideo.getArtist())
                .dotVideoLikesList(dotVideo.getDotVideoLikesList())
                .dotVideoCommentsList(dotVideo.getDotVideoCommentsList())
                .sentDate(dotVideo.getSentDate())
                .viewCount(dotVideo.getViewCount())
                .build();

        dotVideoRepository.save(updatedDotVideo);
    }

    private void updateDotVideoElementCommentsCount(DotVideo dotVideo,int count){
        DotVideo updatedDotVideo = DotVideo.builder()
                .id(dotVideo.getId())
                .dotVideoCommentsList(dotVideo.getDotVideoCommentsList())
                .dotVideoLikesList(dotVideo.getDotVideoLikesList())
                .dotVideoCommentsList(dotVideo.getDotVideoCommentsList())
                .commentsCount(dotVideo.getCommentsCount()+count)
                .likesCount(dotVideo.getLikesCount())
                .viewCount(dotVideo.getViewCount())
                .accessRange(dotVideo.getAccessRange())
                .link(dotVideo.getLink())
                .artist(dotVideo.getArtist())
                .link(dotVideo.getLink())
                .link720(dotVideo.getLink720())
                .link1080(dotVideo.getLink1080())
                .sentDate(dotVideo.getSentDate())
                .thumbnail(dotVideo.getThumbnail())
                .build();
        dotVideoRepository.save(updatedDotVideo);
    }



}
