package tamtam.mooney.domain.auth.service;

import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.agent.service.UserAgentService;
import tamtam.mooney.domain.auth.dto.AuthSignUpRequestDto;
import tamtam.mooney.domain.auth.dto.AuthLoginRequestDto;
import tamtam.mooney.domain.auth.dto.TokenResponseDto;
import tamtam.mooney.domain.user.service.UserService;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.global.exception.CustomException;
import tamtam.mooney.global.exception.ErrorCode;
import tamtam.mooney.global.security.JwtProvider;
import tamtam.mooney.global.security.RedisService;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {
    private final UserService userService;
    private final RedisService redisService;
    private final JwtProvider jwtProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserAgentService userAgentService;

    @Transactional(readOnly = true)
    public void validateEmailAvailability(String email) {
        if (userService.existsByEmail(email)) {
            throw new CustomException(ErrorCode.RESOURCE_ALREADY_EXISTS);
        }
    }

    public TokenResponseDto signUp(AuthSignUpRequestDto requestDto) {
        validateEmailAvailability(requestDto.email());

        if (!requestDto.termsAgreed()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (!requestDto.password().equals(requestDto.confirmPassword())) {
            throw new CustomException(ErrorCode.PASSWORD_MISMATCH);
        }

        // 새로운 User 생성
        User newUser = userService.createUser(
                requestDto.email(),
                encodePassword(requestDto.password()),
                requestDto.nickname()
        );

        // '무니' Agent를 User에게 추가
        userAgentService.assignDefaultAgentToUser(newUser);

        return generateNewTokenPair(newUser);
    }

    public TokenResponseDto login(AuthLoginRequestDto requestDto) {
        User user = userService.getUserByEmail(requestDto.email());

        if (!passwordEncoder.matches(requestDto.password(), user.getEncryptedPassword())) {
            throw new CustomException(ErrorCode.INVALID_CREDENTIALS);
        }

        return generateNewTokenPair(user);
    }

    public TokenResponseDto refreshAccessToken(String refreshToken) {
        jwtProvider.validateRefreshToken(refreshToken);

        Claims claims = jwtProvider.getRefreshTokenClaims(refreshToken);
        String userId = claims.getSubject();
        User user = userService.getUserById(Long.parseLong(userId));
        String redisKey = "auth:refresh_token:" + userId;
        Object storedRefreshToken = redisService.getValues(redisKey);

        if (storedRefreshToken == null || !storedRefreshToken.toString().equals(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        // 기존 리프레시 토큰 삭제
        redisService.deleteValues(redisKey);

        // 새 액세스 토큰 및 리프레시 토큰 생성
        return generateNewTokenPair(user);
    }

    // 토큰 생성 및 저장
    private TokenResponseDto generateNewTokenPair(User user) {
        String accessToken = jwtProvider.generateAccessToken(user.getUserId(), user.getRole().name());
        String refreshToken = jwtProvider.generateRefreshToken(user.getUserId(), user.getRole().name());
        return new TokenResponseDto(accessToken, refreshToken);
    }

    private String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}