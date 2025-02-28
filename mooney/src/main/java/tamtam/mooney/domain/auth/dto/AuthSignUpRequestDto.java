package tamtam.mooney.domain.auth.dto;

import jakarta.validation.constraints.NotEmpty;

public record AuthSignUpRequestDto (
        @NotEmpty
        String email,
        @NotEmpty
        String password,
        @NotEmpty
        String confirmPassword,
        @NotEmpty
        String nickname
) {
}