package com.tripezzy.booking_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic newBookingTopic() {
        return new NewTopic("new-booking", 3, (short) 1);
    }

    @Bean
    public NewTopic updateBookingStatusTopic() {
        return new NewTopic("update-booking-status", 3, (short) 1);
    }

    @Bean
    public NewTopic bookingConfirmedTopic() {
        return new NewTopic("booking-confirmed", 3, (short) 1);
    }
}
