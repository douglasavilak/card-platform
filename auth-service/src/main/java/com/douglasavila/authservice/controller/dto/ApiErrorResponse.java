package com.douglasavila.authservice.controller.dto;

public record ApiErrorResponse(int status, String error, String message) {
}
