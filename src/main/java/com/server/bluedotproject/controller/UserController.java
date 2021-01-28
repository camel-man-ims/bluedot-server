package com.server.bluedotproject.controller;

import com.server.bluedotproject.dto.ApiMessage;
import com.server.bluedotproject.dto.request.*;
import com.server.bluedotproject.dto.response.GenreApiResponse;
import com.server.bluedotproject.dto.response.UserApiResponse;
import com.server.bluedotproject.dto.response.UserHasGenreApiResponse;
import com.server.bluedotproject.dto.response.UserRoleApiResponse;
import com.server.bluedotproject.entity.Genre;
import com.server.bluedotproject.entity.Role;
import com.server.bluedotproject.entity.User;
import com.server.bluedotproject.entity.UserHasGenre;
import com.server.bluedotproject.security.JwtTokenProvider;
import com.server.bluedotproject.service.GenreService;
import com.server.bluedotproject.service.SessionService;
import com.server.bluedotproject.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Api(tags = "유저")
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final GenreService genreService;
    private final SessionService sessionService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    @ApiOperation(value = "회원가입 가능 이메일 체크", notes = "회원가입 가능하면 true, 불가능하면 false 반환")
    @PostMapping("/email-check")
    public ApiMessage<Map<String,Boolean>> emailCheck(@RequestBody LoginCheckApiRequest loginCheckApiRequest) {

        if(userService.emailIsPresent(loginCheckApiRequest.getEmail())){
            return ApiMessage.RESPONSE(ApiMessage.Status.NOT_MODIFIED,createReturnResultUsingMap("isPossible",false));
        }else{
            return ApiMessage.RESPONSE(ApiMessage.Status.NOT_MODIFIED,createReturnResultUsingMap("isPossible",true));
        }
    }

    @ApiOperation(value = "유저 생성", notes = "insert userId를 반환")
    @PostMapping("")
    public ApiMessage<Map<String,Long>> createUser(@Valid @RequestBody UserApiRequest userApiRequest) {

        Map<String, Long> result = new HashMap<>();

        Long insertUserId = userService.createUser(userApiRequest).getId();

        result.put("user_id", insertUserId);

        return ApiMessage.RESPONSE(ApiMessage.Status.CREATED, result);
    }

    @ApiOperation(value = "유저 취향 목록 반환")
    @GetMapping("/genre")
    public ApiMessage<List<GenreApiResponse>> getGenreListOfUser(HttpServletRequest request){

        // parameter : access_token
        Long userId = getUserIdFromHttpServletRequest(request);

        List<GenreApiResponse> genreApiResponseList = new ArrayList<>();

        List<UserHasGenre> getGenreListOfUser = userService.getGenreListOfUser(userId);

            getGenreListOfUser.forEach(item->{
                Genre genre = item.getGenre();
                GenreApiResponse genreApiResponse = GenreApiResponse.builder()
                        .genreId(genre.getId())
                        .genreName(genre.getName())
                        .build();

                genreApiResponseList.add(genreApiResponse);
            });
            return ApiMessage.RESPONSE(ApiMessage.Status.OK,genreApiResponseList);
    }

    @ApiOperation(value = "유저 취향 수정 ",notes = "genre 값을 string 으로 받는다")
    @PutMapping("/genre")
    public ApiMessage<UserHasGenreApiResponse> updateUserGenre(HttpServletRequest request,@RequestBody UserGenreApiRequest userGenreApiRequest ){

        Long userId = getUserIdFromHttpServletRequest(request);

        List<UserHasGenre> updatedUserHasGenreList = userService.updateUserGenre(userId, userGenreApiRequest.getGenreNameList());

        List<String> genreNameList = new ArrayList<>();

        updatedUserHasGenreList.forEach(item->genreNameList.add(item.getGenre().getName()));

        UserHasGenreApiResponse userHasGenreApiResponse = UserHasGenreApiResponse.builder()
                .updatedGenre(genreNameList)
                .build();

        return ApiMessage.RESPONSE(ApiMessage.Status.CREATED,userHasGenreApiResponse);
    }

    @ApiOperation(value = "비밀번호 재설정")
    @PutMapping("/password")
    public ApiMessage<Map<String,Long>> updateUserPassword(HttpServletRequest request,@RequestBody UserPasswordApiRequest userPasswordApiRequest){
        Long userId = getUserIdFromHttpServletRequest(request);
        Long updatedId = userService.updateUserPassword(userPasswordApiRequest.getPassword(), userId).getId();

        Map<String,Long> result = new HashMap<>();

        result.put("updated_id",updatedId);

        return ApiMessage.RESPONSE(ApiMessage.Status.OK,result);
    }

    @ApiOperation(value="유저 권한 체크",notes="")
    @PostMapping("/auth")
    public ApiMessage<UserRoleApiResponse> checkUserAuth(@Nullable HttpServletRequest request){
        Long userId = getUserIdFromHttpServletRequest(request);

        Role userRole = userService.getUserRole(userId);

        UserRoleApiResponse userRoleApiResponse = UserRoleApiResponse.builder()
                .userRole(userRole.getRoleName())
                .userId(userId)
                .build();
        return ApiMessage.RESPONSE(ApiMessage.Status.OK,userRoleApiResponse);
    }

    @ApiOperation(value = "유저 물감 갯수 가져오기")
    @GetMapping("/paint")
    public ApiMessage<Map<String,Integer>> getUserPaintCount(HttpServletRequest request){
        Long userId = getUserIdFromHttpServletRequest(request);
        Integer paintCount = userService.getPaintCount(userId);
        return ApiMessage.RESPONSE(ApiMessage.Status.OK,createReturnResultUsingMap("user_paint_count",paintCount));
    }

    @ApiOperation(value = "유저 프로필 정보 가져오기")
    @GetMapping("/profile")
    public ApiMessage<UserApiResponse> getUserProfile(HttpServletRequest request){
        Long userId = getUserIdFromHttpServletRequest(request);
        User user = userService.getUser(userId);
        UserApiResponse userApiResponse = UserApiResponse.builder()
                .name(user.getName())
                .nickname(user.getNickname())
                .createdAt(user.getCreatedAt())
                .email(user.getEmail())
                .followingCount(user.getFollowingCount())
                .build();
        return ApiMessage.RESPONSE(ApiMessage.Status.OK,userApiResponse);
    }



    @ApiOperation(value = "로그인", notes = "insert email, password -> name, accessToken")
    @PostMapping("/login")
    public ApiMessage<Map<String,String>> loginWithCreateJWT(
            @RequestBody SessionApiRequest resource) throws URISyntaxException {

        String email = resource.getEmail();
        String password = resource.getPassword();

        //(email,password) -> (userId, name)
        User user = sessionService.authenticate(email,password);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email,password)
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtTokenProvider.generateToken(authentication);

//        String url = "/user/" + user.getId();
//
//        ResponseEntity createUser = ResponseEntity.created(new URI(url)).body(
//                SessionApiResponse.builder()
//                        .name(user.getName())
//                        .accessToken(jwt)
//                        .build());

        return ApiMessage.RESPONSE(ApiMessage.Status.CREATED, createReturnResultUsingMap("access_token",jwtTokenProvider.generateToken(authentication)));

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
}
