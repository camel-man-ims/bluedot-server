package com.server.bluedotproject.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Artist{

    @Id
    @Column(name = "artist_id")
    private Long id;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY,cascade = CascadeType.ALL)
    @JoinColumn(name = "artist_id", referencedColumnName="user_id")
    private User user;

    private String description;

    private String profileImg;

    private String bannerImg;

    private Integer averageCanvasTime;

    private Integer followedCount;

    private Integer paintNeedAmount;

    @OneToMany(mappedBy = "artist")
    private List<DotVideo> dotVideoList;

    @OneToMany(mappedBy = "artist")
    private List<ArtistHasGenre> artistHasGenreList;

    @OneToMany(mappedBy = "artist")
    private List<Canvas> canvasList;

    @OneToMany(mappedBy = "artist")
    private List<Post> postList;

    @OneToMany(mappedBy = "artist")
    private List<Payment> paymentList;

}
