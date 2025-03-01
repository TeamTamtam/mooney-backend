package tamtam.mooney.domain.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AuthSignUpRequestDto (
        @NotNull(message = "동의가 필수입니다.")
        Boolean termsAgreed,
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