package com.tripezzy.payment_service.grpc.server;

import com.tripezzy.payment_service.dto.PaymentsResponse;
import com.tripezzy.payment_service.grpc.EmptyRequest;
import com.tripezzy.payment_service.grpc.PaymentServiceGrpc;
import com.tripezzy.payment_service.grpc.PaymentsResponseList;
import com.tripezzy.payment_service.grpc.UserPaymentsRequest;
import com.tripezzy.payment_service.service.PaymentService;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;



@GrpcService
public class PaymentGrpcService extends PaymentServiceGrpc.PaymentServiceImplBase {


    private static final Logger log = LoggerFactory.getLogger(PaymentGrpcService.class);
    private final PaymentService paymentService;

    public PaymentGrpcService(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Override
    public void getAllPayments(EmptyRequest request, StreamObserver<PaymentsResponseList> responseObserver) {
        List<PaymentsResponse> payments = paymentService.getAllPayments();

        PaymentsResponseList responseList = PaymentsResponseList.newBuilder()
                .addAllPayments(payments.stream().map(this::convertToGrpcPayment).collect(Collectors.toList()))
                .build();

        responseObserver.onNext(responseList);
        responseObserver.onCompleted();
    }

    @Override
    public void getAllPaymentsByUserId(UserPaymentsRequest request, StreamObserver<PaymentsResponseList> responseObserver) {
        List<PaymentsResponse> payments = paymentService.getAllPaymentsByUserId(request.getUserId());

        PaymentsResponseList responseList = PaymentsResponseList.newBuilder()
                .addAllPayments(payments.stream().map(this::convertToGrpcPayment).collect(Collectors.toList()))
                .build();

        responseObserver.onNext(responseList);
        responseObserver.onCompleted();
    }

    private com.tripezzy.payment_service.grpc.PaymentsResponse convertToGrpcPayment(PaymentsResponse dto) {
        return com.tripezzy.payment_service.grpc.PaymentsResponse.newBuilder()
                .setId(dto.getId())
                .setUserId(dto.getUserId())
                .setReferenceId(dto.getReferenceId())
                .setSessionId(dto.getSessionId())
                .setStatus(dto.getStatus().name())
                .setAmount(dto.getAmount())
                .setCurrency(dto.getCurrency())
                .setName(dto.getName())
                .setCategory(dto.getCategory().name())
                .setQuantity(dto.getQuantity())
                .setCreatedAt(dto.getCreatedAt().toString())
                .build();
    }
}
