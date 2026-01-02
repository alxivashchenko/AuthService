package com.alexivashchenko.auth.service.exception;

public record ApiError(
        String code,
        String message
) {}

