package tamtam.mooney.domain.auth.dto;

public record TokenResponseDto (
    String accessToken,
    String refreshToken
) {
}