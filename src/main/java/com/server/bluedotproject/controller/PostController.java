package com.server.bluedotproject.controller;

import com.server.bluedotproject.component.Uploader;
import com.server.bluedotproject.dto.ApiMessage;
import com.server.bluedotproject.dto.request.PostCommentsApiRequest;
import com.server.bluedotproject.dto.request.PostCommentsUpdateApiRequest;
import com.server.bluedotproject.dto.request.PostGenreNameApiRequest;
import com.server.bluedotproject.dto.response.PostApiResponse;
import com.server.bluedotproject.dto.response.PostCommentsApiResponse;
import com.server.bluedotproject.dto.response.SinglePostApiResponse;
import com.server.bluedotproject.entity.Post;
import com.server.bluedotproject.entity.PostComments;
import com.server.bluedotproject.entity.PostCommentsLikes;
import com.server.bluedotproject.entity.enumclass.AccessRange;
import com.server.bluedotproject.security.JwtTokenProvider;
import com.server.bluedotproject.service.PostService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api(tags = "게시글")
@RestController
@RequestMapping("/post")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final JwtTokenProvider jwtTokenProvider;
    private final Uploader uploader;

    @ApiOperation(value = "게시글 생성")
    @PostMapping("")
    public ApiMessage<Map<String,Long>> createPost(HttpServletRequest request,
                                                   @RequestParam("video_file") MultipartFile file,
                                                   @RequestParam("access_range") AccessRange accessRange,
                                                   @RequestParam("description") String description,
                                                   @RequestParam("title") String title) throws IOException {
        Long userId = getUserIdFromHttpServletRequest(request);

        String videoURL = uploader.upload(file, "videos");

        String[] parsingFileName = file.getOriginalFilename().split("\\.");

        String thumbnailURL = uploader.getThumbnail(parsingFileName[0]);
        String thumbnailResult = insertPlusToFileName(thumbnailURL);

        // 비디오 변환된 것을 가져온다
        List<String> videoURLParsingList = uploader.getConvertedVideos(parsingFileName[0]);
        List<String> videoURLInsertPlus = new ArrayList<>();

        videoURLParsingList.forEach(item -> videoURLInsertPlus.add(insertPlusToFileName(item)));

        Post createdPost = postService.createPost(userId, title, videoURL, thumbnailResult, accessRange, description);

        return ApiMessage.RESPONSE(ApiMessage.Status.CREATED,createReturnResultUsingMap("insertedPostId",createdPost.getId()));
    }

    @ApiOperation(value = "조회수 기준으로 전체 게시글 가져오기",notes = "조회수 50 이상만")
    @GetMapping("/viewcount")
    public ApiMessage<List<PostApiResponse>> getPostByViewCount(HttpServletRequest request,@PageableDefault(size=100) Pageable pageable){

        Long userId = getUserIdFromHttpServletRequest(request);

        List<Post> arrangedPost = postService.getPostListByViewCount(pageable);

        List<PostApiResponse> postApiResponseList = getPostApiResponse(arrangedPost);

        return ApiMessage.RESPONSE(ApiMessage.Status.OK,postApiResponseList);
    }

    // pageable 에러...
    @ApiOperation(value = "최신순 기준으로 전체 게시글 가져오기")
    @GetMapping("/newest")
    public ApiMessage<List<PostApiResponse>> getPostByCreatedAt(@PageableDefault(size=100) Pageable pageable){

        List<Post> arrangedPost = postService.getPostListByCreatedAt(pageable);

        List<PostApiResponse> postApiResponseList = getPostApiResponse(arrangedPost);
        return ApiMessage.RESPONSE(ApiMessage.Status.OK,postApiResponseList);
    }

    @ApiOperation(value = "전체 게시글 가져오기")
    @GetMapping("/all")
    public ApiMessage<List<PostApiResponse>> getAllPost(){

        List<Post> findAllPost = postService.getAllPost();

        List<PostApiResponse> postApiResponseList = getPostApiResponse(findAllPost);
        return ApiMessage.RESPONSE(ApiMessage.Status.OK,postApiResponseList);
    }

    // 수정하기
    @ApiOperation(value= "장르별 전체 게시글 가져오기")
    @GetMapping("/genre")
    public ApiMessage<List<PostApiResponse>> getPostByGenre(@RequestBody PostGenreNameApiRequest postGenreNameApiRequest,@PageableDefault(size=10) Pageable pageable){

        List<Post> getPostListByGenre = postService.getPostListByGenreName(postGenreNameApiRequest.getGenreName(),pageable);

        List<PostApiResponse> postApiResponseList = getPostApiResponse(getPostListByGenre);
        return ApiMessage.RESPONSE(ApiMessage.Status.OK,postApiResponseList);
    }


    @ApiOperation(value = "개별 게시글 가져오기")
    @GetMapping("/single/{postId}")
    public ApiMessage<SinglePostApiResponse> getSinglePost(@PathVariable Long postId){
        Post post = postService.getSinglePost(postId);


        List<PostComments> postCommentsList = postService.getPostCommentsListByPost(post);
        List<PostCommentsApiResponse> postCommentsApiResponseList = new ArrayList<>();

        // 댓글 list
        postCommentsList.forEach(item->{
            PostCommentsApiResponse postCommentsApiResponse = PostCommentsApiResponse.builder()
                    .postCommentsId(item.getId())
                    .comments(item.getComments())
                    .createdAt(item.getCreatedAt())
                    .likesCount(item.getLikesCount())
                    .updatedAt(item.getUpdatedAt())
                    .userId(item.getUser().getId())
                    .build();
            postCommentsApiResponseList.add(postCommentsApiResponse);
        });

        SinglePostApiResponse singlePostApiResponse = SinglePostApiResponse.builder()
                .postId(post.getId())
                .accessRange(post.getAccessRange())
                .artistId(post.getArtist().getId())
                .createdAt(post.getCreatedAt())
                .description(post.getDescription())
                .link(post.getLink())
                .thumbnail(post.getThumbnail())
                .title(post.getTitle())
                .viewCount(post.getViewCount())
                .postCommentsList(postCommentsApiResponseList)
                .build();

        return ApiMessage.RESPONSE(ApiMessage.Status.OK,singlePostApiResponse);
    }
    // <--> 게시글 댓글 Create Update Delete <--> //
    @ApiOperation(value = "게시글 댓글 생성")
    @PostMapping("/comments/{postId}")
    public ApiMessage<Map<String,Integer>> createPostComments(HttpServletRequest request,@PathVariable Long postId,@RequestBody PostCommentsApiRequest postCommentsApiRequest){
        Long userId = getUserIdFromHttpServletRequest(request);
        PostComments insertedPostComments = postService.createPostComments(postCommentsApiRequest,userId,postId);
        // 생성된 게시글 댓글의 아이디를 넘기면, 게시글의 최신 댓글수를 반환
        Integer postElementCommentsCount = postService.getPostElementCommentsCount(insertedPostComments.getPost());
        // Map 에 PostId 와 postElementCommentCount 와 같이 Response 에 담아서 보낸다
        Map<String, Integer> result = createReturnResultUsingMap("post_element_comments_count", postElementCommentsCount);
        result.putAll(createReturnResultUsingMap("post_id", insertedPostComments.getPost().getId().intValue()));
        return ApiMessage.RESPONSE(ApiMessage.Status.CREATED,result);
    }


    @ApiOperation(value = "게시글 댓글 수정")
    @PutMapping("/comments/{postCommentsId}")
    public ApiMessage<Map<String,PostCommentsApiResponse>> updatePostComments(HttpServletRequest request,@PathVariable Long postCommentsId,@RequestBody PostCommentsUpdateApiRequest postCommentsUpdateApiRequest){
        Long userId = getUserIdFromHttpServletRequest(request);
        PostComments updatedPostComments = postService.updatePostComments(postCommentsUpdateApiRequest,userId,postCommentsId);

        PostCommentsApiResponse postCommentsApiResponse = PostCommentsApiResponse.builder()
                .userId(updatedPostComments.getUser().getId())
                .updatedAt(updatedPostComments.getUpdatedAt())
                .likesCount(updatedPostComments.getLikesCount())
                .createdAt(updatedPostComments.getCreatedAt())
                .comments(updatedPostComments.getComments())
                .postCommentsId(updatedPostComments.getId())
                .build();

        return ApiMessage.RESPONSE(ApiMessage.Status.OK,createReturnResultUsingMap("updated_post_comment",postCommentsApiResponse));
    }

    @ApiOperation(value = "게시글 댓글 삭제")
    @DeleteMapping("/comments/{postCommentsId}")
    public ApiMessage<Map<String,Integer>> deletePostComments(HttpServletRequest request,@PathVariable Long postCommentsId){
        Long userId = getUserIdFromHttpServletRequest(request);

        Post postBeforeDeleted = postService.getPostByPostCommentsId(postCommentsId);
        postService.deletePostComments(postCommentsId,userId);

        // 지우기 전에 저장해놨던 Post 객체를 넘기면 게시글의 최신 댓글수를 반환
        Integer postElementCommentsCount = postService.getPostElementCommentsCount(postBeforeDeleted);
        Map<String, Integer> result = createReturnResultUsingMap("post_element_comments_count", postElementCommentsCount);
        result.putAll(createReturnResultUsingMap("post_id", postBeforeDeleted.getId().intValue()));
        return ApiMessage.RESPONSE(ApiMessage.Status.OK,result);
    }

    // <--> 게시글 댓글 좋아요 Create Delete --> //
    @ApiOperation(value = "게시글 댓글 좋아요 생성")
    @PostMapping("/comments/likes/{postCommentsId}")
    public ApiMessage<Map<String,Integer>> createCommentsLikes(HttpServletRequest request,@PathVariable Long postCommentsId){
        Long userId = getUserIdFromHttpServletRequest(request);
        PostCommentsLikes insertedPostCommentsLikes = postService.createPostCommentsLikes(postCommentsId,userId);
        Integer postCommentsElementLikesCount = postService.getPostCommentsElementLikesCount(insertedPostCommentsLikes.getPostComments());

        Map<String, Integer> result = createReturnResultUsingMap("post_comments_element_likes_count", postCommentsElementLikesCount);
        result.putAll(createReturnResultUsingMap("postComments_id", insertedPostCommentsLikes.getPostComments().getId().intValue()));

        return ApiMessage.RESPONSE(ApiMessage.Status.CREATED,result);
    }

    @ApiOperation(value = "게시글 댓글 좋아요 삭제")
    @DeleteMapping("/comments/likes/{postCommentsId}")
    public ApiMessage<Map<String,Integer>> deleteCommentsLikes(HttpServletRequest request,@PathVariable Long postCommentsId){
        Long userId = getUserIdFromHttpServletRequest(request);
        PostComments postCommentsBeforeDeleted = postService.getPostCommentsByPostCommentsId(postCommentsId);
        postService.deletePostCommentsLikesUsingPostCommentsIdAndUserId(postCommentsId,userId);
        Integer postCommentsElementLikesCount = postService.getPostCommentsElementLikesCount(postCommentsBeforeDeleted);

        Map<String, Integer> result = createReturnResultUsingMap("postComments_element_likes_count", postCommentsElementLikesCount);
        result.putAll(createReturnResultUsingMap("postComments_id", postCommentsBeforeDeleted.getId().intValue()));

        return ApiMessage.RESPONSE(ApiMessage.Status.CREATED,result);
    }

    @ApiOperation(value = "게시글 검색",notes = "제목 + 본문")
    @GetMapping("/search")
    public ApiMessage<List<PostApiResponse>> searchPosts(@RequestParam String keyword){

        List<PostApiResponse> postApiResponseList = new ArrayList<>();

        postService.searchPost(keyword).forEach(item->{
            PostApiResponse postApiResponse = PostApiResponse.builder()
                    .postId(item.getId())
                    .viewCount(item.getViewCount())
                    .title(item.getTitle())
                    .thumbnail(item.getThumbnail())
                    .link(item.getLink())
                    .description(item.getDescription())
                    .createdAt(item.getCreatedAt())
                    .artistId(item.getArtist().getId())
                    .accessRange(item.getAccessRange())
                    .commentsCount(item.getCommentsCount())
                    .likesCount(item.getLikesCount())
                    .build();
            postApiResponseList.add(postApiResponse);
        });

        return ApiMessage.RESPONSE(ApiMessage.Status.OK,postApiResponseList);
    }


    // PostApiResponse DTO 반환
    private List<PostApiResponse> getPostApiResponse(List<Post> postList){
        List<PostApiResponse> postApiResponseList = new ArrayList<>();

        postList.forEach(item->{
            PostApiResponse postApiResponse = PostApiResponse.builder()
                    .postId(item.getId())
                    .accessRange(item.getAccessRange())
                    .artistId(item.getArtist().getId())
                    .createdAt(item.getCreatedAt())
                    .description(item.getDescription())
                    .link(item.getLink())
                    .viewCount(item.getViewCount())
                    .commentsCount(item.getCommentsCount())
                    .likesCount(item.getLikesCount())
                    .thumbnail(item.getThumbnail())
                    .title(item.getTitle())
                    .viewCount(item.getViewCount())
                    .build();

            postApiResponseList.add(postApiResponse);
        });
        return postApiResponseList;
    }
    public static <T> Map<String,T> createReturnResultUsingMap(String insertDataAttribute, T insertData){
        Map<String,T> returnValue = new HashMap<>();

        returnValue.put(insertDataAttribute,insertData);

        return returnValue;
    }
    public Long getUserIdFromHttpServletRequest(HttpServletRequest request){
        String token = request.getHeader("access_token");
        return jwtTokenProvider.getUserIdFromJwt(token);
    }

    // S3 파일 이름 마지막에 +가 붙는다. 그래서 파일 이름에 +를 붙여줌
    public String insertPlusToFileName(String item){
        String[] split = item.split("/");
        split[split.length-1] = "+" + split[split.length-1];

        String splitResult = null;

        for(String a : split){
            splitResult = splitResult + a + "/";
        }
        return splitResult.substring(4,splitResult.length()-1);
    }
}


