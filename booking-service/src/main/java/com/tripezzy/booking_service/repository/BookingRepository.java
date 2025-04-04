package com.tripezzy.booking_service.repository;

import com.tripezzy.booking_service.entity.Booking;
import com.tripezzy.booking_service.entity.enums.PaymentStatus;
import com.tripezzy.booking_service.entity.enums.Status;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> , JpaSpecificationExecutor<Booking> {

    @Cacheable("bookingsByUserId")
    Page<Booking> findByUser(Long userId, Pageable pageable);

    @Cacheable("bookingsByDestinationId")
    Page<Booking> findByDestination(Long destinationId, Pageable pageable);

    @Cacheable("bookingsByStatus")
    Page<Booking> findByStatus(Status status, Pageable pageable);

    @Cacheable("bookingsByPaymentStatus")
    List<Booking> findByPaymentStatus(PaymentStatus paymentStatus);

    @Query("SELECT COUNT(b) FROM Booking b WHERE b.status = :status AND b.deleted = false")
    long countByStatus(@Param("status") Status status);

    @Query("SELECT b FROM Booking b WHERE b.status = :status AND b.travelDate > CURRENT_DATE AND b.deleted = false")
    List<Booking> findUpcomingBookingsByStatus(@Param("status") Status status);

    @Query("SELECT b FROM Booking b WHERE b.paymentStatus = :paymentStatus AND b.totalPrice BETWEEN :minPrice AND :maxPrice AND b.deleted = false")
    List<Booking> findBookingsByPaymentStatusAndTotalPriceRange(
            @Param("paymentStatus") PaymentStatus paymentStatus,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice
    );

    @Modifying
    @Query("UPDATE Booking b SET b.deleted = true, b.status = :status WHERE b.id = :id")
    void softDeleteById(@Param("id") Long id, @Param("status") Status status);
}