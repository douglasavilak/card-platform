package com.douglasavila.authservice.controller.dto;

public record LoginResponse(String accessToken, Long expiresIn) {
}
