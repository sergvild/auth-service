package com.vizavi.authserver.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleTokenRequest(@NotBlank String idToken) {}