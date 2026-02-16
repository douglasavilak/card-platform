package com.douglasavila.cardservice.controller.dto;

public record ApiErrorResponse(int status, String error, String message) {
}
