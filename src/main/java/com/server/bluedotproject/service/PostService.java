package com.server.bluedotproject.service;

import com.server.bluedotproject.dto.request.PostCommentsApiRequest;
import com.server.bluedotproject.dto.request.PostCommentsUpdateApiRequest;
import com.server.bluedotproject.entity.*;
import com.server.bluedotproject.entity.enumclass.AccessRange;
import com.server.bluedotproject.entity.repository.*;
import com.server.bluedotproject.exceptions.AuthorizationException;
import com.server.bluedotproject.exceptions.DuplicateException;
import com.server.bluedotproject.exceptions.ErrorCode;
import com.server.bluedotproject.exceptions.NotExistException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final ArtistRepository artistRepository;
    private final ArtistHasGenreRepository artistHasGenreRepository;
    private final GenreRepository genreRepository;
    private final PostCommentsRepository postCommentsRepository;
    private final UserRepository userRepository;
    private final PostCommentsLikesRepository postCommentsLikesRepository;

    @Transactional(rollbackFor = {NotExistException.class})
    public Post createPost(Long userId,String title ,String videoURL, String thumbnailURL, AccessRange accessRange,String description){

        Artist artist = artistRepository.findById(userId).orElseThrow(()->new NotExistException(ErrorCode.ARTIST_DOES_NOT_EXIST));

            Post newPost = Post.builder()
                .artist(artist)
                .link(videoURL)
                .thumbnail(thumbnailURL)
                .title(title)
                .description(description)
                .accessRange(accessRange)
                .viewCount(0)
                .commentsCount(0)
                .likesCount(0)
                .createdAt(LocalDateTime.now())
                .build();

            return postRepository.save(newPost);
    }

    public List<Post> getPostListByViewCount(Pageable pageable){

        Page<Post> findAllPostPageList = postRepository.findAll(pageable);
        List<Post> findAllPost = findAllPostPageList.getContent();

        // 50 이상인 post 만 반환
        List<Post> filterOverFiftyPost = findAllPost.stream().filter(item->item.getViewCount()>=50).collect(Collectors.toList());

        // viewCount 에 따라서 sorting
        sortingPostByViewCount(filterOverFiftyPost);

        return filterOverFiftyPost;

    }

    public List<Post> getPostListByCreatedAt(Pageable pageable){
            List<Post> findAllPost = postRepository.findAll();
//            Page<Post> pageList = postRepository.findAll(pageable);
//            List<Post> why = pageList.getContent();
//
            // 날짜가 최신인 것이 먼저 return
            sortingPostByCreatedAt(findAllPost);

            return findAllPost;
    }

    public List<Post> getAllPost(){
       return postRepository.findAll();
    }

    public List<Post> getPostListByGenreName(String genreName,Pageable pageable){

        Genre genre = genreRepository.findByName(genreName).orElseThrow(()->new NotExistException(ErrorCode.GENRE_DOES_NOT_EXIST));

        List<ArtistHasGenre> artistHasGenre = artistHasGenreRepository.findByGenre(genre,pageable);

        List<Post> postList = new ArrayList<>();

        artistHasGenre.forEach(item->{
            // ArtistHasGenre 에서 artist 를 찾는다
            Artist findArtist = item.getArtist();
            // PostList 에 Artist 가 갖고 있는 모든 게시글을 추가한다
            postList.addAll(findArtist.getPostList());
        });

        return postList;
    }

    public Post getSinglePost(Long postId){
        return postRepository.findById(postId).orElseThrow(()->new NotExistException(ErrorCode.POST_DOES_NOT_EXIST));
    }

    public PostComments createPostComments(PostCommentsApiRequest postCommentsApiRequest,Long userId,Long postId){
        Post post = postRepository.findById(postId).orElseThrow(()->new NotExistException(ErrorCode.POST_DOES_NOT_EXIST));
        User user = userRepository.findById(userId).orElseThrow(()->new NotExistException(ErrorCode.USER_DOES_NOT_EXIST));

        PostComments postComments = PostComments.builder()
                .post(post)
                .user(user)
                .createdAt(LocalDateTime.now())
                .likesCount(0)
                .comments(postCommentsApiRequest.getComments())
                .build();

        updatePostCommentsCount(post,1);

        return postCommentsRepository.save(postComments);
    }

    public PostComments updatePostComments(PostCommentsUpdateApiRequest postCommentsUpdateApiRequest,Long userId,Long postCommentsId){

        PostComments findBeforePostComments = postCommentsRepository.findById(postCommentsId).orElseThrow(()->new NotExistException(ErrorCode.POST_COMMENTS_DOES_NOT_EXIST));

        // 유저가 쓴 글인지 확인
        if(!findBeforePostComments.getUser().getId().equals(userId)){
            throw new AuthorizationException(ErrorCode.AUTHORIZATION_ERROR);
        }

        PostComments postComments = PostComments.builder()
                .id(findBeforePostComments.getId())
                .post(findBeforePostComments.getPost())
                .user(findBeforePostComments.getUser())
                .updatedAt(LocalDateTime.now())
                .createdAt(findBeforePostComments.getCreatedAt())
                .likesCount(findBeforePostComments.getLikesCount())
                .comments(postCommentsUpdateApiRequest.getComments()) // 코멘트 수정
                .build();

        return postCommentsRepository.save(postComments);
    }

    public Long deletePostComments(Long postCommentsId,Long userId){
        PostComments postComments = postCommentsRepository.findById(postCommentsId).orElseThrow(()->new NotExistException(ErrorCode.POST_COMMENTS_DOES_NOT_EXIST));

        if(!postComments.getUser().getId().equals(userId)){
            throw new AuthorizationException(ErrorCode.AUTHORIZATION_ERROR);
        }

        postComments.getPostCommentsLikesList().forEach(postCommentsLikesRepository::delete);
        postCommentsRepository.delete(postComments);

        updatePostCommentsCount(postComments.getPost(),-1);

        return postComments.getId();
    }

    public PostCommentsLikes createPostCommentsLikes(Long postCommentsId,Long userId){
        User user = userRepository.findById(userId).orElseThrow(()->new NotExistException(ErrorCode.USER_DOES_NOT_EXIST));
        PostComments postComments = postCommentsRepository.findById(postCommentsId).orElseThrow(()->new NotExistException(ErrorCode.POST_DOES_NOT_EXIST));

        postCommentsLikesRepository.findByUserAndPostComments(user,postComments).ifPresent(postCommentsLikes->{throw new DuplicateException(ErrorCode.DUPLICATE_POST_LIKES);});

            PostCommentsLikes postCommentsLikes = PostCommentsLikes.builder()
                    .user(user)
                    .postComments(postComments)
                    .build();

            updatePostElementCommentsLikesCount(postComments,1);

        return postCommentsLikesRepository.save(postCommentsLikes);
    }

    public Long deletePostCommentsLikesUsingPostCommentsIdAndUserId(Long postCommentsId, Long userId){

        User user = userRepository.findById(userId).orElseThrow(()->new NotExistException(ErrorCode.USER_DOES_NOT_EXIST));
        PostComments postComments = postCommentsRepository.findById(postCommentsId).orElseThrow(()->new NotExistException(ErrorCode.POST_COMMENTS_DOES_NOT_EXIST));

        PostCommentsLikes postCommentsLikes = postCommentsLikesRepository.findByUserAndPostComments(user,postComments).orElseThrow(()->new NotExistException(ErrorCode.POST_COMMENTS_LIKES_DOES_NOT_EXIST));

        if(!postCommentsLikes.getUser().getId().equals(userId)){
            throw new AuthorizationException(ErrorCode.AUTHORIZATION_ERROR);
        }

        postCommentsLikesRepository.delete(postCommentsLikes);

        updatePostElementCommentsLikesCount(postCommentsLikes.getPostComments(),-1);

        return postCommentsLikes.getId();
    }

    public List<PostComments> getPostCommentsListByPost(Post post){
        return postCommentsRepository.findByPost(post);
    }

    public List<Post> searchPost(String keyword){
        List<Post> findPostByTitleAndResult = postRepository.findByTitleContaining(keyword);
        List<Post> findPostByDescription = postRepository.findByDescriptionContaining(keyword);

        for(Post post : findPostByDescription){
            if(!findPostByTitleAndResult.contains(post)){
                findPostByTitleAndResult.add(post);
            }
        }
        return findPostByTitleAndResult;
    }


    public Integer getPostElementCommentsCount(Post post){
        return postRepository.findById(post.getId()).orElseThrow(()->new NotExistException(ErrorCode.POST_DOES_NOT_EXIST)).getCommentsCount();
    }
    public Integer getPostElementLikesCount(Long postId){
        Post post = postRepository.findById(postId).orElseThrow(()->new NotExistException(ErrorCode.POST_DOES_NOT_EXIST));
        return post.getLikesCount();
    }
    public Integer getPostCommentsElementLikesCount(PostComments postComments){
        return postCommentsRepository.findById(postComments.getId()).orElseThrow(()->new NotExistException(ErrorCode.POST_COMMENTS_DOES_NOT_EXIST)).getLikesCount();
    }
    public PostComments getPostCommentsByPostCommentsLikesId(Long postCommentsLikesId){
        return postCommentsLikesRepository.findById(postCommentsLikesId).orElseThrow(()->new NotExistException(ErrorCode.POST_COMMENTS_LIKES_DOES_NOT_EXIST)).getPostComments();
    }

    public Post getPostByPostCommentsId(Long postCommentsId){
        return postCommentsRepository.findById(postCommentsId).orElseThrow(()->new NotExistException(ErrorCode.POST_COMMENTS_DOES_NOT_EXIST)).getPost();
    }

    public PostComments getPostCommentsByPostCommentsId(Long postCommentsId) {
        return postCommentsRepository.findById(postCommentsId).orElseThrow(()->new NotExistException(ErrorCode.DOT_VIDEO_COMMENTS_DOES_NOT_EXIST));
    }




    // <ㅡㅡ method ㅡㅡ> //

    private void updatePostElementCommentsLikesCount(PostComments postComments, int count){
        PostComments updatedPostComments = PostComments.builder()
                .id(postComments.getId())
                .comments(postComments.getComments())
                .postCommentsLikesList(postComments.getPostCommentsLikesList())
                .post(postComments.getPost())
                .updatedAt(postComments.getUpdatedAt())
                .createdAt(postComments.getCreatedAt())
                .likesCount(postComments.getLikesCount()+count)
                .user(postComments.getUser())
                .build();
        postCommentsRepository.save(updatedPostComments);
    }

    private void updatePostLikesCount(Post post,int count){
        Post updatedPost = Post.builder()
                .id(post.getId())
                .PostCommentsList(post.getPostCommentsList())
                .postLikesList(post.getPostLikesList())
                .updatedAt(post.getUpdatedAt())
                .accessRange(post.getAccessRange())
                .artist(post.getArtist())
                .commentsCount(post.getCommentsCount())
                .createdAt(post.getCreatedAt())
                .description(post.getDescription())
                .likesCount(post.getLikesCount()+count)
                .link(post.getLink())
                .thumbnail(post.getThumbnail())
                .title(post.getTitle())
                .viewCount(post.getViewCount())
                .build();
        postRepository.save(updatedPost);
    }

    private void updatePostCommentsCount(Post post,int count){
        Post updatedPost = Post.builder()
                .id(post.getId())
                .PostCommentsList(post.getPostCommentsList())
                .postLikesList(post.getPostLikesList())
                .updatedAt(post.getUpdatedAt())
                .accessRange(post.getAccessRange())
                .artist(post.getArtist())
                .commentsCount(post.getCommentsCount()+count)
                .createdAt(post.getCreatedAt())
                .description(post.getDescription())
                .likesCount(post.getLikesCount())
                .link(post.getLink())
                .thumbnail(post.getThumbnail())
                .title(post.getTitle())
                .viewCount(post.getViewCount())
                .build();
        postRepository.save(updatedPost);
    }

    private void sortingPostByViewCount(List<Post> list){
        Collections.sort(list, (o1, o2) -> {
            if(o1.getViewCount()<o2.getViewCount()){
                return 1;
            }else if(o1.getViewCount()>o2.getViewCount()){
                return -1;
            }
            return 0;
        });
    }

    private void sortingPostByCreatedAt(List<Post> list){
        Collections.sort(list, (o1, o2) -> {
            if(o1.getCreatedAt().isAfter(o2.getCreatedAt())){
                return -1;
            }else if(o1.getCreatedAt().isBefore(o2.getCreatedAt())){
                return 1;
            }
            return 0;
        });
    }



    // <ㅡㅡ method ㅡㅡ> //

}
