package com.tripezzy.admin_service.controller;

import blog.Blog;
import com.tripezzy.admin_service.annotations.RoleRequired;
import com.tripezzy.admin_service.grpc.BlogGrpcClient;
import com.tripezzy.admin_service.grpc.BookingGrpcClient;
import com.tripezzy.admin_service.grpc.PaymentGrpcClient;
import com.tripezzy.admin_service.grpc.ProductGrpcClient;
import com.tripezzy.booking_service.grpc.Booking;
import com.tripezzy.payment_service.grpc.PaymentsResponse;
import com.tripezzy.product_service.grpc.Product;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/core")
public class AdminController {

    private final BlogGrpcClient blogGrpcClient;
    private final BookingGrpcClient bookingGrpcClient;
    private final ProductGrpcClient productGrpcClient;
    private final PaymentGrpcClient paymentGrpcClient;

    public AdminController(BlogGrpcClient blogGrpcClient, BookingGrpcClient bookingGrpcClient, ProductGrpcClient productGrpcClient, PaymentGrpcClient paymentGrpcClient) {
        this.blogGrpcClient = blogGrpcClient;
        this.bookingGrpcClient = bookingGrpcClient;
        this.productGrpcClient = productGrpcClient;
        this.paymentGrpcClient = paymentGrpcClient;
    }

    @GetMapping("/blogs")
    @RoleRequired("ADMIN")
    public ResponseEntity<List<Blog>> getAllBlogs(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(blogGrpcClient.getAllBlogs(page, size));
    }

    @GetMapping("/bookings/paginated")
    @RoleRequired("ADMIN")
    public List<Booking> getAllBookings(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return bookingGrpcClient.getAllBookings(page, size);
    }

    @GetMapping("/bookings/user/{userId}")
    @RoleRequired("ADMIN")
    public ResponseEntity<List<Booking>> getBookingsByUserId(@PathVariable Long userId,
                                             @RequestParam(defaultValue = "0") int page,
                                             @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookingGrpcClient.getBookingsByUserId(userId, page, size));
    }

    @GetMapping("/bookings/destination/{destinationId}")
    @RoleRequired("ADMIN")
    public ResponseEntity<List<Booking>> getBookingsByDestinationId(@PathVariable Long destinationId,
                                                    @RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookingGrpcClient.getBookingsByDestinationId(destinationId, page, size));
    }

    @DeleteMapping("/soft-delete/{bookingId}")
    @RoleRequired("ADMIN")
    public ResponseEntity<Void> softDeleteBooking(@PathVariable Long bookingId) {
        boolean success = bookingGrpcClient.softDeleteBooking(bookingId);
        return success ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @GetMapping("/payment-status")
    @RoleRequired("ADMIN")
    public ResponseEntity<List<Booking>> getBookingsByPaymentStatus(@RequestParam String paymentStatus) {
        return ResponseEntity.ok(bookingGrpcClient.getBookingsByPaymentStatus(paymentStatus));
    }

    @GetMapping("/status")
    @RoleRequired("ADMIN")
    public ResponseEntity<List<Booking>> getBookingsByStatus(@RequestParam String status,
                                                              @RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(bookingGrpcClient.getBookingsByStatus(status, page, size));
    }

    @PatchMapping("/{bookingId}/confirm")
    @RoleRequired("ADMIN")
    public ResponseEntity<Booking> confirmBooking(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingGrpcClient.confirmBooking(bookingId));
    }

    @GetMapping("/products")
    @RoleRequired("ADMIN")
    public List<Product> getAllProducts(@RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "10") int size) {
        return productGrpcClient.getAllProducts(page, size);
    }

    @GetMapping("/payments")
    @RoleRequired("ADMIN")
    public ResponseEntity<List<PaymentsResponse>> getAllPayments() {
        return ResponseEntity.ok(paymentGrpcClient.getAllPayments());
    }

    @GetMapping("/payments/{userId}")
    @RoleRequired("ADMIN")
    public ResponseEntity<List<PaymentsResponse>> getAllPaymentsByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(paymentGrpcClient.getAllPaymentsByUserId(userId));
    }
}
