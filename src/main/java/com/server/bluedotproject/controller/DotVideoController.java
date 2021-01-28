package com.server.bluedotproject.controller;

import com.server.bluedotproject.component.Uploader;
import com.server.bluedotproject.dto.ApiMessage;
import com.server.bluedotproject.dto.request.DotVideoCommentsApiRequest;
import com.server.bluedotproject.dto.response.ArtistApiResponse;
import com.server.bluedotproject.dto.response.DotVideoApiResponse;
import com.server.bluedotproject.dto.response.DotVideoCommentsApiResponse;
import com.server.bluedotproject.dto.response.DotVideoCommentsPlusCountApi;
import com.server.bluedotproject.entity.*;
import com.server.bluedotproject.entity.enumclass.AccessRange;
import com.server.bluedotproject.security.JwtTokenProvider;
import com.server.bluedotproject.service.ArtistService;
import com.server.bluedotproject.service.DotVideoService;
import com.server.bluedotproject.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Api(tags = "닷비디오")
@RestController
@RequestMapping("/dotvideo")
@RequiredArgsConstructor
public class DotVideoController {

    private final Uploader uploader;
    private final DotVideoService dotVideoService;
    private final JwtTokenProvider jwtTokenProvider;
    private final ArtistService artistService;
    private final UserService userService;

    @ApiOperation(value = "닷비디오 등록",notes = "등록하기")
    @PostMapping("")
    public ApiMessage<Map<String,String>> createDotVideo(
            HttpServletRequest request,
            @RequestParam("video_file") MultipartFile file,
            @RequestParam("access_range") AccessRange accessRange,
            @RequestParam("description") String description
    ) throws IOException {

        Long userId = getUserIdFromHttpServletRequest(request);

        Map<String, String> result = new HashMap<>();

        String videoURL = uploader.upload(file, "videos");

        String[] parsingFileName = file.getOriginalFilename().split("\\.");

        // 파일 이름을 s3에 저장할 수 있게 자름
        String thumbnailURL = uploader.getThumbnail(parsingFileName[0]);

        String thumbnailResult = insertPlusToFileName(thumbnailURL);

        // 비디오 변환된 것을 가져온다
        List<String> videoURLParsingList = uploader.getConvertedVideos(parsingFileName[0]);
        List<String> videoURLInsertPlus = new ArrayList<>();

        videoURLParsingList.forEach(item -> videoURLInsertPlus.add(insertPlusToFileName(item)));

        Long insertId = dotVideoService.createDotVideo(videoURL,videoURLInsertPlus.get(0),videoURLInsertPlus.get(1),thumbnailURL,userId,accessRange,description).getId();

        result.put("video_url", videoURL);
        result.put("thumbnail_url", thumbnailResult);
        result.put("video-1080p", videoURLInsertPlus.get(0));
        result.put("video-720p", videoURLInsertPlus.get(1));
        result.put("insert_id", String.valueOf(insertId));

        return ApiMessage.RESPONSE(ApiMessage.Status.OK, result);
    }

    @ApiOperation(value = "닷비디오 댓글 생성")
    @PostMapping("/comments/{dotVideoId}")
    public ApiMessage<DotVideoCommentsPlusCountApi> createDotVideoComments(HttpServletRequest request,@PathVariable Long dotVideoId,@RequestBody DotVideoCommentsApiRequest dotVideoCommentsApiRequest){
        Long userId = getUserIdFromHttpServletRequest(request);

        DotVideoComments insertedDotVideoComments = dotVideoService.createDotVideoComments(dotVideoId,userId,dotVideoCommentsApiRequest.getComments());

        DotVideoCommentsApiResponse dotVideoCommentsApiResponse = DotVideoCommentsApiResponse.builder()
                .dotVideoCommentsId(insertedDotVideoComments.getId())
                .dotVideoId(insertedDotVideoComments.getDotVideo().getId())
                .comments(insertedDotVideoComments.getComments())
                .createdAt(insertedDotVideoComments.getCreatedAt())
                .updatedAt(insertedDotVideoComments.getUpdatedAt())
                .build();

        Integer dotVideoElementCommentsCount = dotVideoService.getDotVideoElementCommentsCount(insertedDotVideoComments.getDotVideo().getId());

        DotVideoCommentsPlusCountApi result = DotVideoCommentsPlusCountApi.builder()
                .dotVideoCommentsCount(dotVideoElementCommentsCount)
                .dotVideoComments(dotVideoCommentsApiResponse)
                .build();

        return ApiMessage.RESPONSE(ApiMessage.Status.CREATED,result);
    }

    @ApiOperation(value = "닷비디오 댓글 좋아요 생성")
    @PostMapping("/comments/likes/{dotVideoCommentsId}")
    public ApiMessage<Map<String,Integer>> createDotVideoCommentsLikes(HttpServletRequest request,@PathVariable Long dotVideoCommentsId){
        Long userId = getUserIdFromHttpServletRequest(request);

        DotVideoCommentsLikes insertedDotVideoCommentsLikes = dotVideoService.createDotVideoCommentsLikes(dotVideoCommentsId,userId);
        Integer dotVideoCommentsElementLikesCount = dotVideoService.getDotVideoCommentsElementLikesCount(insertedDotVideoCommentsLikes.getDotVideoComments().getId());

        Map<String,Integer> result = createReturnResultUsingMap("dot_video_comments_id",dotVideoCommentsId.intValue());
        result.putAll(createReturnResultUsingMap("dot_video_comments_element_likes_count",dotVideoCommentsElementLikesCount));

        return ApiMessage.RESPONSE(ApiMessage.Status.CREATED,result);
    }

    @ApiOperation(value = "닷비디오 댓글 좋아요 해제")
    @DeleteMapping("/comments/likes/{dotVideoCommentsId}")
    public ApiMessage<Map<String,Integer>> deleteDotVideoCommentsLikes(HttpServletRequest request,@PathVariable Long dotVideoCommentsId){
        Long userId = getUserIdFromHttpServletRequest(request);

        dotVideoService.deleteDotVideoCommentsLikes(dotVideoCommentsId,userId);
        Integer dotVideoCommentsElementLikesCount = dotVideoService.getDotVideoCommentsElementLikesCount(dotVideoCommentsId);

        Map<String,Integer> result = createReturnResultUsingMap("dot_video_comments_element_likes_count",dotVideoCommentsElementLikesCount);

        return ApiMessage.RESPONSE(ApiMessage.Status.OK,result);
    }

    @ApiOperation(value = "닷비디오 좋아요 생성")
    @PostMapping("/likes/{dotVideoId}")
    public ApiMessage<Map<String,Integer>> createDotVideoLikes(HttpServletRequest request,@PathVariable Long dotVideoId){
        Long userId = getUserIdFromHttpServletRequest(request);

        DotVideoLikes insertedDotVideoLikes = dotVideoService.createDotVideoLikes(dotVideoId, userId);
        Integer dotVideoLikesCount = dotVideoService.getDotVideoLikesCount(insertedDotVideoLikes.getDotVideo().getId());

        Map<String,Integer> result = createReturnResultUsingMap("dot_video_element_likes_count",dotVideoLikesCount);

        return ApiMessage.RESPONSE(ApiMessage.Status.CREATED,result);
    }
    @ApiOperation(value = "닷비디오 좋아요 해제")
    @DeleteMapping("/likes/{dotVideoId}")
    public ApiMessage<Map<String,Integer>> deleteDotVideoLikes(HttpServletRequest request,@PathVariable Long dotVideoId){
        Long userId = getUserIdFromHttpServletRequest(request);

        dotVideoService.deleteDotVideoLikes(dotVideoId, userId);
        Integer dotVideoLikesCount = dotVideoService.getDotVideoLikesCount(dotVideoId);

        Map<String,Integer> result = createReturnResultUsingMap("dot_video_element_likes_count",dotVideoLikesCount);

        return ApiMessage.RESPONSE(ApiMessage.Status.OK,result);
    }

    @ApiOperation(value="닷비디오 댓글 삭제")
    @DeleteMapping("/comments/{dotVideoCommentsId}")
    public ApiMessage<Map<String,Integer>> deleteDotVideoComments(HttpServletRequest request,@PathVariable Long dotVideoCommentsId){
        Long userId = getUserIdFromHttpServletRequest(request);

        DotVideo dotVideo = dotVideoService.getDotVideoByDotVideoCommentsId(dotVideoCommentsId);

        dotVideoService.deleteDotVideoComments(dotVideoCommentsId,userId);
        Integer dotVideoElementsCommentsCount = dotVideoService.getDotVideoElementCommentsCount(dotVideo.getId());

        return ApiMessage.RESPONSE(ApiMessage.Status.OK,createReturnResultUsingMap("dot_video_element_comments_count",dotVideoElementsCommentsCount));
    }

    @ApiOperation(value = "닷비디오 댓글 수정")
    @PutMapping("/comments/{dotVideoCommentsId}")
    public ApiMessage<DotVideoCommentsApiResponse> updateDotVideoComments
            (HttpServletRequest request,@PathVariable Long dotVideoCommentsId,@RequestBody DotVideoCommentsApiRequest dotVideoCommentsApiRequest){
        Long userId = getUserIdFromHttpServletRequest(request);

        DotVideoComments updatedDotVideoComments = dotVideoService.updateDotVideoComments(dotVideoCommentsId, userId, dotVideoCommentsApiRequest.getComments());

        DotVideoCommentsApiResponse dotVideoCommentsApiResponse = DotVideoCommentsApiResponse.builder()
                .dotVideoId(updatedDotVideoComments.getDotVideo().getId())
                .dotVideoCommentsId(updatedDotVideoComments.getId())
                .comments(updatedDotVideoComments.getComments())
                .createdAt(updatedDotVideoComments.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
        return ApiMessage.RESPONSE(ApiMessage.Status.OK,dotVideoCommentsApiResponse);
    }

    @ApiOperation(value = "닷 비디오 전체 가져오기",notes = "public 인 dotVideo 만 가져온다")
    @GetMapping("/all")
    public ApiMessage<List<DotVideoApiResponse>> getAllDotVideo(){

        List<DotVideoApiResponse> dotVideoApiResponseList = new ArrayList<>();

        List<DotVideo> dotVideoList = dotVideoService.getAllDotVideo();
        List<DotVideo> publicDotVideoList = dotVideoList.stream().filter(item -> item.getAccessRange() == AccessRange.PUBLIC).collect(Collectors.toList());

        publicDotVideoList.forEach(item->{
            Artist artist = item.getArtist();
            ArtistApiResponse artistApiResponse = getArtistApiResponseByArtist(artist);

            DotVideoApiResponse dotVideoApiResponse = DotVideoApiResponse.builder()
                    .dotVideoId(item.getId())
                    .videoLink(item.getLink())
                    .artist(artistApiResponse)
                    .likesCount(item.getLikesCount())
                    .viewCount(item.getViewCount())
                    .commentsCount(item.getCommentsCount())
                    .build();
            dotVideoApiResponseList.add(dotVideoApiResponse);
        });
        return ApiMessage.RESPONSE(ApiMessage.Status.OK,dotVideoApiResponseList);
    }

    @ApiOperation(value = "닷비디오 개별로 가져오기",notes = "public 인 dotVideo 만 가져온다")
    @GetMapping("/{dotVideoId}")
    public ApiMessage<DotVideoApiResponse> getDotVideo(@PathVariable Long dotVideoId){
        DotVideo findDotVideo = dotVideoService.getDotVideoById(dotVideoId);

        Artist artist = findDotVideo.getArtist();
        ArtistApiResponse artistApiResponse = getArtistApiResponseByArtist(artist);
        DotVideoApiResponse dotVideoApiResponse = DotVideoApiResponse.builder()
                .dotVideoId(findDotVideo.getId())
                .commentsCount(findDotVideo.getCommentsCount())
                .viewCount(findDotVideo.getViewCount())
                .likesCount(findDotVideo.getLikesCount())
                .videoLink(findDotVideo.getLink())
                .artist(artistApiResponse)
                .build();
        return ApiMessage.RESPONSE(ApiMessage.Status.OK,dotVideoApiResponse);
    }

    /*
     9. 닷비디오 검색 가져오기
     10. 내 닷터 비디오 가져오기
    */

    // <----> Method <-------> //

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
    public static <T> Map<String,T> createReturnResultUsingMap(String insertDataAttribute, T insertData){
        Map<String,T> returnValue = new HashMap<>();

        returnValue.put(insertDataAttribute,insertData);

        return returnValue;
    }
    public Long getUserIdFromHttpServletRequest(HttpServletRequest request){
        String token = request.getHeader("access_token");
        return jwtTokenProvider.getUserIdFromJwt(token);
    }
    public ArtistApiResponse getArtistApiResponseByArtist(Artist artist){
        User user = userService.getUserByArtist(artist);
        return ArtistApiResponse.builder()
                .artistId(artist.getId())
                .averageCanvasTime(artist.getAverageCanvasTime())
                .followedCount(artist.getFollowedCount())
                .profileImg(artist.getProfileImg())
                .description(artist.getDescription())
                .name(user.getName())
                .nickname(user.getNickname())
                .description(artist.getDescription())
                .followedCount(artist.getFollowedCount())
                .build();
    }
}
