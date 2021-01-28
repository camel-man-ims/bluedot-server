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
public class DotVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="dot_video_id")
    private Long id;

    private String link;

    private String link1080;

    private String link720;

    private String thumbnail;

    private LocalDateTime sentDate;

    @Enumerated(EnumType.STRING)
    private AccessRange accessRange;

    private Integer viewCount;

    private Integer likesCount;

    private Integer commentsCount;

    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id")
    private Artist artist;

    @OneToMany(mappedBy = "dotVideo")
    private List<DotVideoComments> dotVideoCommentsList;

    @OneToMany(mappedBy = "dotVideo")
    private List<DotVideoLikes> dotVideoLikesList;


}
