package com.server.bluedotproject.service;

import com.server.bluedotproject.entity.Artist;
import com.server.bluedotproject.entity.Payment;
import com.server.bluedotproject.entity.User;
import com.server.bluedotproject.entity.enumclass.AccessRange;
import com.server.bluedotproject.entity.enumclass.ArtistState;
import com.server.bluedotproject.entity.repository.ArtistRepository;
import com.server.bluedotproject.entity.repository.DotVideoRepository;
import com.server.bluedotproject.entity.repository.PaymentRepository;
import com.server.bluedotproject.entity.repository.UserRepository;
import com.server.bluedotproject.exceptions.AuthorizationException;
import com.server.bluedotproject.exceptions.ErrorCode;
import com.server.bluedotproject.exceptions.NotExistException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final DotVideoRepository dotVideoRepository;
    private final UserRepository userRepository;
    private final ArtistRepository artistRepository;

    @Transactional(rollbackFor = {NotExistException.class})
    public Payment createPayment(Long userId, Long artistId, String requestInfo, @Nullable String sendEmail) {

        User user = userRepository.findById(userId).orElseThrow(() -> new NotExistException(ErrorCode.USER_DOES_NOT_EXIST));
        Artist artist = artistRepository.findById(artistId).orElseThrow(()->new NotExistException(ErrorCode.ARTIST_DOES_NOT_EXIST));

        Payment payment = Payment.builder()
                .user(user)
                .artist(artist)
                .sendEmail(sendEmail)
                .accessRange(AccessRange.PUBLIC)
                .artistState(ArtistState.ORDER)
                .cashAmountUsed(artist.getPaintNeedAmount())
                .requestInfo(requestInfo)
                .createdAt(LocalDateTime.now())
                .build();

        minusUserPaint(user,artist.getPaintNeedAmount());

        return paymentRepository.save(payment);
    }

    public Integer checkUserPaymentCount(Long userId, Long artistId) {

        User user = userRepository.findById(userId).orElseThrow(() -> new NotExistException(ErrorCode.USER_DOES_NOT_EXIST));
        Artist artist = artistRepository.findById(artistId).orElseThrow(()->new NotExistException(ErrorCode.ARTIST_DOES_NOT_EXIST));

        List<Payment> userPaymentList = paymentRepository.findByUserAndArtist(user, artist);

        List<Payment> userPaymentListAfterCheckingArtistState = userPaymentList.stream().filter(item -> item.getArtistState() != ArtistState.CANCEL)
                .filter(item -> item.getArtistState() != ArtistState.DONE).collect(Collectors.toList());
        return userPaymentListAfterCheckingArtistState.size();
    }

    public List<Payment> getPaymentListOfArtist(Long artistId) {
        Artist artist = artistRepository.findById(artistId).orElseThrow(()->new NotExistException(ErrorCode.ARTIST_DOES_NOT_EXIST));

        return paymentRepository.findByArtist(artist);
    }

    public Payment updatePaymentArtistState(Long artistId,Long paymentId,ArtistState artistState) {
        checkPaymentIsOfArtist(artistId,paymentId);
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new NotExistException(ErrorCode.PAYMENT_DOES_NOT_EXIST));
        return updateArtistState(payment,artistState);
    }

    public void insertArtistState(Long artistId,Long paymentId,String reason) {
        checkPaymentIsOfArtist(artistId,paymentId);
        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new NotExistException(ErrorCode.PAYMENT_DOES_NOT_EXIST));
        updateCancelReason(payment,reason);
    }





    // <--> method <--> //

    private void minusUserPaint(User user,Integer paint){
        User updatedUser = User.builder()
                .userHasGenreList(user.getUserHasGenreList())
                .createdAt(user.getCreatedAt())
                .password(user.getPassword())
                .dotVideoCommentsList(user.getDotVideoCommentsList())
                .dotVideoLikesList(user.getDotVideoLikesList())
                .email(user.getEmail())
                .followedList(user.getFollowedList())
                .followingCount(user.getFollowingCount())
                .followingList(user.getFollowingList())
                .id(user.getId())
                .isDeleted(user.getIsDeleted())
                .name(user.getName())
                .nickname(user.getNickname())
                .paint(user.getPaint()-paint)
                .paymentList(user.getPaymentList())
                .postCommentsLikesList(user.getPostCommentsLikesList())
                .updatedAt(user.getUpdatedAt())
                .postCommentsList(user.getPostCommentsList())
                .postLikesList(user.getPostLikesList())
                .build();
        userRepository.save(updatedUser);
    }

    private Payment updateCancelReason(Payment payment, String reason){
        Payment updatedPayment = Payment.builder()
                .id(payment.getId())
                .artistState(payment.getArtistState())
                .updatedAt(payment.getUpdatedAt())
                .artist(payment.getArtist())
                .accessRange(payment.getAccessRange())
                .createdAt(payment.getCreatedAt())
                .requestInfo(payment.getRequestInfo())
                .sendEmail(payment.getSendEmail())
                .cashAmountUsed(payment.getCashAmountUsed())
                .videoLink(payment.getVideoLink())
                .cancelReason(reason)
                .user(payment.getUser())
                .build();
        return paymentRepository.save(updatedPayment);
    }
    private Payment updateArtistState(Payment payment, ArtistState artistState){
        Payment updatedPayment = Payment.builder()
                .id(payment.getId())
                .artistState(artistState)
                .updatedAt(payment.getUpdatedAt())
                .artist(payment.getArtist())
                .accessRange(payment.getAccessRange())
                .createdAt(payment.getCreatedAt())
                .requestInfo(payment.getRequestInfo())
                .sendEmail(payment.getSendEmail())
                .cashAmountUsed(payment.getCashAmountUsed())
                .videoLink(payment.getVideoLink())
                .cancelReason(payment.getCancelReason())
                .user(payment.getUser())
                .build();
        return paymentRepository.save(updatedPayment);
    }

    private void checkPaymentIsOfArtist(Long artistId, Long paymentId){
        Artist artist = artistRepository.findById(artistId).orElseThrow(()->new NotExistException(ErrorCode.ARTIST_DOES_NOT_EXIST));

        Payment payment = paymentRepository.findById(paymentId).orElseThrow(() -> new NotExistException(ErrorCode.PAYMENT_DOES_NOT_EXIST));

        if(!artist.getId().equals(payment.getArtist().getId())){
            throw new AuthorizationException(ErrorCode.ARTIST_PAYMENT_AUTHORIZATION_ERROR);
        }
    }


}
