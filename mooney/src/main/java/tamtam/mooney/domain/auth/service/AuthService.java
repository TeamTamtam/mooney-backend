package tamtam.mooney.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.auth.dto.AuthSignUpRequestDto;
import tamtam.mooney.domain.auth.dto.AuthLoginRequestDto;
import tamtam.mooney.domain.auth.dto.TokenResponseDto;
import tamtam.mooney.domain.user.service.UserService;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.global.exception.CustomException;
import tamtam.mooney.global.exception.ErrorCode;
import tamtam.mooney.global.security.JwtProvider;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public void validateEmailAvailability(String email) {
        if (userService.existsByEmail(email)) {
            throw new CustomException(ErrorCode.RESOURCE_ALREADY_EXISTS);
        }
    }

    public TokenResponseDto signUp(AuthSignUpRequestDto requestDto) {
        validateEmailAvailability(requestDto.email());

        if (!requestDto.password().equals(requestDto.confirmPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
        }

        User newUser = userService.createUser(
                requestDto.email(),
                encodePassword(requestDto.password()),
                requestDto.nickname()
        );

        return generateTokenResponse(newUser);
    }

    public TokenResponseDto login(AuthLoginRequestDto requestDto) {
        User user = userService.getUserByEmail(requestDto.email());

        if (!passwordEncoder.matches(requestDto.password(), user.getEncryptedPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        return generateTokenResponse(user);
    }

    public TokenResponseDto refreshAccessToken(String refreshToken) {
        Long userId = jwtProvider.validateToken(refreshToken);
        User user = userService.getUserById(userId);

        if (!user.getRefreshToken().equals(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        String newAccessToken = jwtProvider.createAccessToken(userId);
        return new TokenResponseDto(newAccessToken, refreshToken);
    }

    // 토큰 생성 및 저장
    private TokenResponseDto generateTokenResponse(User user) {
        String accessToken = jwtProvider.createAccessToken(user.getUserId());
        String refreshToken = jwtProvider.createRefreshToken(user.getUserId());

        userService.updateRefreshToken(user.getUserId(), refreshToken);
        return new TokenResponseDto(accessToken, refreshToken);
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}
