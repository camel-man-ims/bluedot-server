package com.server.bluedotproject.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    // 아티스트 ( artist )
    ARTIST_DOES_NOT_EXIST(400,"아티스트가 존재하지 않습니다"),
    THIS_YOUR_IS_NOT_A_ARTIST(403,"이 유저는 아티스트 등록이 되지 않았습니다"),
    EMAIL_CONFIRM_NOT_ALLOWED(404, "이메일 인증에 실패했습니다."),
    ROLE_NAME_DOES_NOT_EXIST(401,"권한 에러"),
    // 팔로우 ( follow )
    FOLLOWED_USER_DOES_NOT_EXIST(400,"Followed 유저가 없습니다"),
    FOLLOWING_USER_DOES_NOT_EXIST(400,"Following 유저가 없습니다"),
    NOT_FOLLOWING_RELATION(400,"팔로잉 관계가 아닙니다"),
    DUPLICATE_FOLLOWING_RELATION(400,"중복된 팔로잉 관계입니다"),
    // 유저 ( user )
    USER_DOES_NOT_EXIST(400, "유저가 존재하지 않습니다"),
    USER_EMAIL_ALREADY_EXIST(400,"유저 이메일이 이미 있습니다"),
    USER_NICKNAME_ALREADY_EXIST(400,"유저 닉네임이 이미 있습니다"),
    USER_CREATE_FAIL(500,"유저 생성에 실패했습니다"),
    USER_HAS_GENRE_NOT_EXIST(400,"유저 취향이 존재하지 않습니다"),
    USER_PASSWORD_WRONG(400, "비밀번호가 틀렸습니다"),
    USER_EMAIL_CHECK(400,"유저 이메일 존재하지 않습니다"),
    USER_MAKES_ROLE_ERROR(500,"유저 권한을 생성하는데 실패했습니다"),
    USER_HAS_ROLE_DOES_NOT_EXIST(500,"유저의 권한 값이 없습니다"),
    // 장르 ( genre )
    GENRE_DOES_NOT_EXIST(400,"장르가 존재하지 않습니다"),
    // 게시글 ( post )
    POST_DOES_NOT_EXIST(400,"게시글이 존재하지 않습니다"),
    POST_COMMENTS_DOES_NOT_EXIST(400,"게시글 댓글이 존재하지 않습니다"),
    DUPLICATE_POST_LIKES(400,"중복된 좋아요 관계입니다"),
    POST_COMMENTS_LIKES_DOES_NOT_EXIST(404,"게시글 좋아요가 존재하지 않습니다"),
    // 닷비디오 ( dotVideo )
    DOT_VIDEO_DOES_NOT_EXIST(400,"닷비디오가 존재하지 않습니다"),
    DOT_VIDEO_COMMENTS_DOES_NOT_EXIST(400,"닷비디오 댓글이 존재하지 않습니다"),
    AUTHORIZATION_ERROR(403,"이 유저가 작성한 글이 아닙니다"),
    DOT_VIDEO_LIKES_DOES_NOT_EXIST(400,"닷비디오 좋아요가 존재하지 않습니다"),
    // 결제 ( payment )
    CANNOT_REQUEST_OVER_THIRD_TIME(403,"이 유저는 이미 한 아티스트에 대해서 3번의 요청을 다 했습니다"),
    PAYMENT_DOES_NOT_EXIST(500,"결제 내역이 존재하지 않습니다"),
    ARTIST_PAYMENT_AUTHORIZATION_ERROR(500,"이 아티스트와 관련된 결제 목록이 아닙니다"),
    ARTIST_STATE_ONLY_ALLOW_TWO(400,"요청 상태에서 아티스트 상태는 제작중 / 취소 두 가지만 가능합니다"),
    //역할
    ROLE_DOES_NOT_EXIST(404, "역할이 존재하지 않습니다"),
    ROLE_DOES_NOT_ACCEPTED(404, "해당 역할은 승인되지 않습니다"),

    //인가
    USER_NOT_AUTHORIZED(404, "JWT 인가에 실패하였습니다"),
    INVALID_JWT_SIGNATURE(404, "SECRET KEY가 올바르지 않습니다"),
    INVALID_JWT_TOKEN(404, "토큰의 구조가 올바르지 않습니다"),
    EXPIRED_JWT_TOKEN(404, "토큰의 유효 기간이 만료되었습니다"),
    UNSUPPORTED_JWT_TOKEN(404, "수신한 JWT형식이 해당 애플리케이션과 일치하지 않습니다"),
    JWT_CLAIMS_IS_EMPTY(404, "JWT CLAIMS가 비어있습니다")

            ;

    private final int status;
    private final String message;

}