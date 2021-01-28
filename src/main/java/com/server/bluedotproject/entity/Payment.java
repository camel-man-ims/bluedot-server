package com.server.bluedotproject.entity;

import com.server.bluedotproject.entity.enumclass.AccessRange;
import com.server.bluedotproject.entity.enumclass.ArtistState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
public class Payment {

    @Id
    @Column(name="payment_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String requestInfo;

    private Integer cashAmountUsed;

    private String sendEmail;

    private String videoLink;

    private String cancelReason;

    @Enumerated(EnumType.STRING)
    private AccessRange accessRange;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Enumerated(EnumType.STRING)
    private ArtistState artistState;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "artist_id")
    private Artist artist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

}
