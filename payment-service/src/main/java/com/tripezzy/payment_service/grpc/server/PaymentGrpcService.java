package com.tripezzy.payment_service.grpc.server;

import com.tripezzy.payment_service.dto.PaymentsResponse;
import com.tripezzy.payment_service.exceptions.ResourceNotFound;
import com.tripezzy.payment_service.grpc.EmptyRequest;
import com.tripezzy.payment_service.grpc.PaymentServiceGrpc;
import com.tripezzy.payment_service.grpc.PaymentsResponseList;
import com.tripezzy.payment_service.grpc.UserPaymentsRequest;
import com.tripezzy.payment_service.service.PaymentService;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
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
        try {
            log.info("Processing gRPC request for getAllPayments");

            List<PaymentsResponse> payments = paymentService.getAllPayments();

            PaymentsResponseList response = PaymentsResponseList.newBuilder()
                    .addAllPayments(payments.stream()
                            .map(this::convertToGrpcPayment)
                            .collect(Collectors.toList()))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("Successfully processed getAllPayments request");

        } catch (StatusRuntimeException e) {
            log.error("gRPC error in getAllPayments: {}", e.getStatus().getDescription(), e);
            responseObserver.onError(e);
        } catch (Exception e) {
            log.error("Unexpected error in getAllPayments", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    @Override
    public void getAllPaymentsByUserId(UserPaymentsRequest request, StreamObserver<PaymentsResponseList> responseObserver) {
        try {
            log.info("Processing gRPC request for getAllPaymentsByUserId - userId: {}", request.getUserId());

            if (request.getUserId() <= 0) {
                throw Status.INVALID_ARGUMENT
                        .withDescription("Invalid user ID")
                        .asRuntimeException();
            }

            List<PaymentsResponse> payments = paymentService.getAllPaymentsByUserId(request.getUserId());

            PaymentsResponseList response = PaymentsResponseList.newBuilder()
                    .addAllPayments(payments.stream()
                            .map(this::convertToGrpcPayment)
                            .collect(Collectors.toList()))
                    .build();

            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("Successfully processed getAllPaymentsByUserId request");

        } catch (ResourceNotFound e) {
            log.warn("No payments found for user ID: {}", request.getUserId());
            responseObserver.onError(Status.NOT_FOUND
                    .withDescription(e.getMessage())
                    .asRuntimeException());
        } catch (StatusRuntimeException e) {
            log.error("gRPC error in getAllPaymentsByUserId: {}", e.getStatus().getDescription(), e);
            responseObserver.onError(e);
        } catch (Exception e) {
            log.error("Unexpected error in getAllPaymentsByUserId", e);
            responseObserver.onError(Status.INTERNAL
                    .withDescription("Internal server error")
                    .withCause(e)
                    .asRuntimeException());
        }
    }

    private com.tripezzy.payment_service.grpc.PaymentsResponse convertToGrpcPayment(PaymentsResponse dto) {
        try {
            return com.tripezzy.payment_service.grpc.PaymentsResponse.newBuilder()
                    .setId(dto.getId())
                    .setUserId(dto.getUser())
                    .setReferenceId(dto.getReference())
                    .setSessionId(dto.getSession())
                    .setStatus(dto.getStatus().name())
                    .setAmount(dto.getAmount())
                    .setCurrency(dto.getCurrency())
                    .setName(dto.getName())
                    .setCategory(dto.getCategory().name())
                    .setQuantity(dto.getQuantity())
                    .setCreatedAt(dto.getCreatedAt().toString())
                    .build();
        } catch (Exception e) {
            log.error("Failed to convert payment ID {} to gRPC response", dto.getId(), e);
            throw Status.INTERNAL
                    .withDescription("Failed to process payment data")
                    .asRuntimeException();
        }
    }
}
