package com.tripezzy.notification_service.dto;

import java.io.Serializable;

public class NotificationDto implements Serializable {
    private Long id;
    private String message;

    public NotificationDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
