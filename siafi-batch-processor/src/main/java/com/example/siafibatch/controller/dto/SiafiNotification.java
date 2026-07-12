package com.example.siafibatch.controller.dto;

import java.time.LocalDateTime;

public record SiafiNotification(
    String id,
    String message,
    String type, // SUCCESS, ERROR, INFO
    LocalDateTime timestamp,
    boolean read
) {}
