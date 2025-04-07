package com.tripezzy.payment_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic checkoutProductTopic() {
        return new NewTopic("checkout-product", 3, (short) 1);
    }
}
