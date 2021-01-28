package com.server.bluedotproject.entity;

import com.server.bluedotproject.entity.enumclass.AccessRange;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Post {

    @Id
    @Column(name="post_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String link;

    private String thumbnail;

    private String title;

    private String description;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private AccessRange accessRange;

    private Integer viewCount;

    private Integer commentsCount;

    private Integer likesCount;

    @OneToMany(mappedBy = "post")
    private List<PostLikes> postLikesList;

    @OneToMany(mappedBy = "post")
    private List<PostComments> PostCommentsList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id")
    private Artist artist;
}
