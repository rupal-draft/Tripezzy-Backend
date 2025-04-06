package com.tripezzy.booking_service.events;

public class BookingConfirmedEvent {

    private Long booking;
    private Long user;

    public BookingConfirmedEvent() {
    }

    public Long getBooking() {
        return booking;
    }

    public void setBooking(Long booking) {
        this.booking = booking;
    }

    public Long getUser() {
        return user;
    }

    public void setUser(Long user) {
        this.user = user;
    }
}
