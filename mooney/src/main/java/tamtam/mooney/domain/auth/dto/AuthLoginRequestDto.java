package tamtam.mooney.domain.auth.dto;

import jakarta.validation.constraints.NotEmpty;

public record AuthLoginRequestDto(
        @NotEmpty
        String email,
        @NotEmpty
        String password
) {
}