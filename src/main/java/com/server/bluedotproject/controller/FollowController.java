package com.server.bluedotproject.controller;

import com.server.bluedotproject.dto.ApiMessage;
import com.server.bluedotproject.dto.response.ArtistApiResponse;
import com.server.bluedotproject.entity.Artist;
import com.server.bluedotproject.entity.Follow;
import com.server.bluedotproject.entity.User;
import com.server.bluedotproject.security.JwtTokenProvider;
import com.server.bluedotproject.service.ArtistService;
import com.server.bluedotproject.service.FollowService;
import com.server.bluedotproject.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Api(tags = "팔로우")
@RequestMapping("/follow")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;
    private final UserService userService;
    private final ArtistService artistService;
    private final JwtTokenProvider jwtTokenProvider;

    @ApiOperation(value = "팔로우 관계 생성")
    @PostMapping("/{followedId}")
    public ApiMessage<Map<String,Long>> createFollow(HttpServletRequest request,@PathVariable Long followedId){

        Long userId = getUserIdFromHttpServletRequest(request);

        Map<String,Long> result = new HashMap<>();

        Long insertId = followService.createFollow(followedId,userId).getId();

        result.put("insert_id",insertId);

        return ApiMessage.RESPONSE(ApiMessage.Status.CREATED,result);
    }

    @ApiOperation(value = "팔로우 관계 해제")
    @DeleteMapping("/{followedId}")
    public ApiMessage<Map<String,Long>> deleteFollow (HttpServletRequest request,@PathVariable Long followedId){

        Long userId = getUserIdFromHttpServletRequest(request);

        Map<String,Long> result = new HashMap<>();

        Long deletedId = followService.deleteFollowUsingFollowingIdAndFollowedId(userId,followedId);

        result.put("deleted_id",deletedId);

        return ApiMessage.RESPONSE(ApiMessage.Status.OK,result);
    }

    @ApiOperation(value = "유저가 팔로잉 하고 있는 아티스트 목록 가져오기",notes = "userId 필요")
    @GetMapping("/user")
    public ApiMessage<List<ArtistApiResponse>> findFollowingListOfUser (HttpServletRequest request,@PageableDefault(size=10) Pageable pageable){

        Long userId = getUserIdFromHttpServletRequest(request);

        List<ArtistApiResponse> artistApiResponseList = new ArrayList<>();

         List<Follow> followingList = followService.findFollowingListOfUser(userId,pageable);

        followingList.stream().forEach(item -> {

            Artist artist  = artistService.findArtistById(item.getFollowedUser().getId());

               User user = userService.findUser(artist.getId());

                ArtistApiResponse artistApiResponse = ArtistApiResponse.builder()
                        .artistId(artist.getId())
                        .description(artist.getDescription())
                        .profileImg(artist.getProfileImg())
                        .followedCount(artist.getFollowedCount())
                        .nickname(user.getNickname())
                        .name(user.getName())
                        .averageCanvasTime(artist.getAverageCanvasTime())
                        .build();

                artistApiResponseList.add(artistApiResponse);
        });

        return ApiMessage.RESPONSE(ApiMessage.Status.OK,artistApiResponseList);
    }

    public Long getUserIdFromHttpServletRequest(HttpServletRequest request){
        String token = request.getHeader("access_token");
        return jwtTokenProvider.getUserIdFromJwt(token);
    }

}
