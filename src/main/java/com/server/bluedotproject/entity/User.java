package com.server.bluedotproject.entity;

import com.server.bluedotproject.entity.enumclass.IsDeleted;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
public class User {

    @Id
    @Column(name = "user_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

    private String name;

    private String nickname;

    private Integer followingCount;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private IsDeleted isDeleted;

    private Integer paint;

    //-- 회원 email 인증 --//
    private String emailCheckToken;

    private LocalDateTime emailConfirmedAt;

    private boolean emailVerified;

    private String profileImg;

    @OneToMany(mappedBy = "followingUser")
    private List<Follow> followingList;

    @OneToMany(mappedBy = "followedUser")
    private List<Follow> followedList;

    @OneToMany(mappedBy = "user")
    private List<PostLikes> postLikesList;

    @OneToMany(mappedBy = "user")
    private List<DotVideoLikes> dotVideoLikesList;

    @OneToMany(mappedBy = "user")
    private List<DotVideoComments> dotVideoCommentsList;

    @OneToMany(mappedBy = "user")
    private List<PostComments> postCommentsList;

    @OneToMany(mappedBy = "user")
    private List<Payment> paymentList;

    @OneToMany(mappedBy = "user")
    private List<UserHasGenre> userHasGenreList;

    @OneToMany(mappedBy = "user")
    private List<PostCommentsLikes> postCommentsLikesList;

    @OneToMany(mappedBy = "user")
    private List<DotVideoCommentsLikes> dotVideoCommentsLikesList;

    @OneToMany(mappedBy = "user")
    private Set<UserHasRole> userRoles;

    // 이메일 토큰 랜덤 생성
    public void generateEmailCheckToken() {
        this.emailCheckToken = UUID.randomUUID().toString();
    }
    // 이메일 인증 확인
    public void completeEmailConfirm() {
        this.emailVerified = true;
        this.emailConfirmedAt = LocalDateTime.now();
    }
}

