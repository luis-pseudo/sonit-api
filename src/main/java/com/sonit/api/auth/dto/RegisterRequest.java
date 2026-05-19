package com.sonit.api.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "displayName es requerido")
    private String displayName;

    @Email(message = "email debe ser válido")
    @NotBlank(message = "email es requerido")
    private String email;

    @Size(min = 8, message = "password debe tener mínimo 8 caracteres")
    private String password;
}
