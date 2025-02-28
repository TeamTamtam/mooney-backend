package tamtam.mooney.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthSignUpRequestDto (
        @NotBlank
        String email,
        @NotBlank
        String password,
        @NotBlank
        String confirmPassword,
        @NotBlank
        String nickname
) {
}