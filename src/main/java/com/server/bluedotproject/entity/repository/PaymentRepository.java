package com.server.bluedotproject.entity.repository;

import com.server.bluedotproject.entity.Artist;
import com.server.bluedotproject.entity.Payment;
import com.server.bluedotproject.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment,Long> {
    List<Payment> findByUserAndArtist(User user, Artist artist);

    List<Payment> findByArtist(Artist artist);

    List<Payment> findByUser(Long userId);
}
