package com.server.bluedotproject.controller;

import com.server.bluedotproject.component.Uploader;
import com.server.bluedotproject.dto.ApiMessage;
import com.server.bluedotproject.dto.request.ArtistHasGenreApiRequest;
import com.server.bluedotproject.dto.response.ArtistApiResponse;
import com.server.bluedotproject.entity.Artist;
import com.server.bluedotproject.entity.User;
import com.server.bluedotproject.security.JwtTokenProvider;
import com.server.bluedotproject.service.ArtistService;
import com.server.bluedotproject.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Api(tags = {"아티스트"})
@RequestMapping("/artist")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;
    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;
    private final Uploader uploader;

    @ApiOperation(value = "아티스트 등록")
    @PostMapping("")
    public ApiMessage<Map<String,Long>> createArtist(HttpServletRequest request,
                                                     @RequestParam("description") String description,
                                                     @RequestParam("banner_img") @Nullable MultipartFile bannerImg,
                                                     @RequestParam("profile_img") MultipartFile profileImg,
                                                     @RequestParam("genre_name_list") List<String> genreNameList
                                                     ) throws IOException {

        Long userId = getUserIdFromHttpServletRequest(request);

        String bannerImgURL=null;

        if(bannerImg!=null){
            bannerImgURL = uploader.upload(bannerImg, "images");
        }
        String profileImgURL = uploader.upload(profileImg, "images");

        Long insertId = artistService.createArtist(userId,description,bannerImgURL,profileImgURL,genreNameList).getId();

        Map<String,Long> result = new HashMap<>();

        result.put("artistId",insertId);

        return ApiMessage.RESPONSE(ApiMessage.Status.CREATED,result);
    }

    @ApiOperation(value = "아티스트 장르 생성")
    @PostMapping("/genre")
    public ApiMessage createArtistHasGenre(@RequestBody ArtistHasGenreApiRequest artistHasGenreApiRequest){
        artistService.createArtistHasGenre(artistHasGenreApiRequest);
        return ApiMessage.RESPONSE(ApiMessage.Status.CREATED);
    }


    @ApiOperation(value = "모든 아티스트 가져오기")
    @GetMapping("/all")
    public ApiMessage<List<ArtistApiResponse>> getAllArtist(@PageableDefault(size = 100) Pageable pageable){

        List<ArtistApiResponse> artistApiResponseList = new ArrayList<>();

        List<Artist> artistList = artistService.getAllArtist(pageable);

        artistList.forEach(item->{
            ArtistApiResponse artistApiResponse = getArtistDTO(item);
            artistApiResponseList.add(artistApiResponse);
        });
        return ApiMessage.RESPONSE(ApiMessage.Status.OK,artistApiResponseList);
    }

    @ApiOperation(value = "장르별 아티스트 가져오기")
    @GetMapping("/genre/{genre}")
    public ApiMessage<List<ArtistApiResponse>> getArtistByGenre(@PathVariable String genre){
        return artistService.getArtistByGenre(genre);
    }

    @ApiOperation(value = "아티스트 프로필 정보 가져오기")
    @GetMapping("/profile")
    public ApiMessage<ArtistApiResponse> getArtistProfile(HttpServletRequest request){
        Long artistId = getUserIdFromHttpServletRequest(request);
        Artist artist = artistService.getArtist(artistId);
        return ApiMessage.RESPONSE(ApiMessage.Status.OK,getArtistDTO(artist));
    }


    // <----> method <----> //


    public static <T> Map<String,T> createReturnResultUsingMap(String insertDataAttribute, T insertData){
        Map<String,T> returnValue = new HashMap<>();

        returnValue.put(insertDataAttribute,insertData);

        return returnValue;
    }

    public Long getUserIdFromHttpServletRequest(HttpServletRequest request){
        String token = request.getHeader("access_token");
        return jwtTokenProvider.getUserIdFromJwt(token);
    }
    public ArtistApiResponse getArtistDTO(Artist artist){
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
