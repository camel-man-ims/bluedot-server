package com.server.bluedotproject.controller;

import com.server.bluedotproject.dto.ApiMessage;
import com.server.bluedotproject.dto.request.PaymentApiRequest;
import com.server.bluedotproject.dto.request.PaymentArtistCancelReasonApiRequest;
import com.server.bluedotproject.dto.request.PaymentArtistStateApiRequest;
import com.server.bluedotproject.dto.response.PaymentApiResponse;
import com.server.bluedotproject.entity.Payment;
import com.server.bluedotproject.entity.enumclass.ArtistState;
import com.server.bluedotproject.exceptions.AuthorizationException;
import com.server.bluedotproject.exceptions.ErrorCode;
import com.server.bluedotproject.exceptions.InputException;
import com.server.bluedotproject.security.JwtTokenProvider;
import com.server.bluedotproject.service.PaymentService;
import com.server.bluedotproject.service.UserService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;

    @ApiOperation(value = "유저가 아티스트에게 요청")
    @PostMapping("/user/{artistId}")
    public ApiMessage<Map<String,Long>> createPayment(HttpServletRequest request, @PathVariable Long artistId, @RequestBody PaymentApiRequest paymentApiRequest){
        Long userId = getUserIdFromHttpServletRequest(request);
        Integer checkUserPaymentCount = paymentService.checkUserPaymentCount(userId, artistId);

        if(checkUserPaymentCount>=3)
            throw new AuthorizationException(ErrorCode.CANNOT_REQUEST_OVER_THIRD_TIME);

        Long insertedPaymentId = paymentService.createPayment(userId, artistId, paymentApiRequest.getRequestInfo(), paymentApiRequest.getSendEmail()).getId();
        return ApiMessage.RESPONSE(ApiMessage.Status.CREATED,createReturnResultUsingMap("inserted_payment_id",insertedPaymentId));
    }

    @ApiOperation(value = "유저가 아티스트에게 몇 번의 요청을 보냈는지 체크")
    @PostMapping("/user/check-payment/{artistId}")
    public ApiMessage<Map<String,Integer>> checkUserPayment(HttpServletRequest request,@PathVariable Long artistId){
        Long userId = getUserIdFromHttpServletRequest(request);
        Integer userPaymentCount = paymentService.checkUserPaymentCount(userId, artistId);
        return ApiMessage.RESPONSE(ApiMessage.Status.OK,createReturnResultUsingMap("user_payment_count",userPaymentCount));
    }

    @ApiOperation(value = "아티스트 자신의 결제 목록을 가져오기")
    @GetMapping("/artist")
    public ApiMessage<List<PaymentApiResponse>> getPaymentOfArtist(HttpServletRequest request){
        Long artistId = getUserIdFromHttpServletRequest(request);
        List<Payment> paymentListOfArtist = paymentService.getPaymentListOfArtist(artistId);

        List<PaymentApiResponse> paymentListOfArtistDTO = createPaymentListDTO(paymentListOfArtist);

        return ApiMessage.RESPONSE(ApiMessage.Status.OK, paymentListOfArtistDTO);
    }

    @ApiOperation(value = "유저 자신의 결제 정보 가져오기")
    @GetMapping("/user")
    public ApiMessage<List<PaymentApiResponse>> getPaymentOfUser(HttpServletRequest request){
        Long userId = getUserIdFromHttpServletRequest(request);
        List<Payment> paymentListOfUser = userService.getPaymentListOfUser(userId);

        List<PaymentApiResponse> paymentListOfArtistDTO = createPaymentListDTO(paymentListOfUser);

        return ApiMessage.RESPONSE(ApiMessage.Status.OK, paymentListOfArtistDTO);
    }

    @ApiOperation(value = "아티스트가 유저의 요청상태 수정",notes="제작중,취소 두가지 가능")
    @PutMapping("/artist/user-request/{paymentId}")
    public ApiMessage<PaymentApiResponse> updatePaymentUserRequest(HttpServletRequest request, @PathVariable Long paymentId, @RequestBody PaymentArtistStateApiRequest paymentArtistStateApiRequest){
        Long artistId = getUserIdFromHttpServletRequest(request);

        ArtistState artistState = paymentArtistStateApiRequest.getArtistState();

        if( artistState.equals(ArtistState.CANCEL)  || artistState.equals(ArtistState.MAKING)){
            Payment payment = paymentService.updatePaymentArtistState(artistId, paymentId, artistState);

            return ApiMessage.RESPONSE(ApiMessage.Status.OK,createPaymentDTO(payment));
        }else{
            throw new InputException(ErrorCode.ARTIST_STATE_ONLY_ALLOW_TWO);
        }
    }

    @ApiOperation(value = "아티스트의 취소 사유 등록")
    @PostMapping("/artist/cancel-reason/{paymentId}")
    public ApiMessage saveCancelReason(HttpServletRequest request, @PathVariable Long paymentId, @RequestBody PaymentArtistCancelReasonApiRequest paymentArtistCancelReasonApiRequest){
        String reason = paymentArtistCancelReasonApiRequest.getCancelReason();
        Long artistId = getUserIdFromHttpServletRequest(request);

        paymentService.insertArtistState(artistId,paymentId,reason);
        return ApiMessage.RESPONSE(ApiMessage.Status.CREATED);
    }



    // <-----> method <------> //

    public PaymentApiResponse createPaymentDTO(Payment payment){
        return PaymentApiResponse.builder()
                .artistState(payment.getArtistState())
                .artistId(payment.getArtist().getId())
                .accessRange(payment.getAccessRange())
                .userId(payment.getUser().getId())
                .createdAt(payment.getCreatedAt())
                .requestInfo(payment.getRequestInfo())
                .cashAmountUsed(payment.getCashAmountUsed())
                .sendEmail(payment.getSendEmail())
                .build();
    }

    public List<PaymentApiResponse> createPaymentListDTO(List<Payment> paymentList){
        List<PaymentApiResponse> paymentApiResponseList = new ArrayList<>();

        paymentList.forEach(item->{
            PaymentApiResponse paymentApiResponse = PaymentApiResponse.builder()
                    .artistState(item.getArtistState())
                    .artistId(item.getArtist().getId())
                    .accessRange(item.getAccessRange())
                    .userId(item.getUser().getId())
                    .createdAt(item.getCreatedAt())
                    .requestInfo(item.getRequestInfo())
                    .cashAmountUsed(item.getCashAmountUsed())
                    .sendEmail(item.getSendEmail())
                    .build();
            paymentApiResponseList.add(paymentApiResponse);
        });
        return paymentApiResponseList;
    }

    public Long getUserIdFromHttpServletRequest(HttpServletRequest request){
        String token = request.getHeader("access_token");
        return jwtTokenProvider.getUserIdFromJwt(token);
    }
    public static <T> Map<String,T> createReturnResultUsingMap(String insertDataAttribute, T insertData){
        Map<String,T> returnValue = new HashMap<>();

        returnValue.put(insertDataAttribute,insertData);

        return returnValue;
    }
}
