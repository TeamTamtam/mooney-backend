package tamtam.mooney.global.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.domain.user.repository.UserRepository;
import tamtam.mooney.global.exception.CustomException;
import tamtam.mooney.global.exception.ErrorCode;

import java.security.Key;
import java.time.Duration;
import java.util.Date;

@Component
public class JwtProvider {
    private final Key accessKey;
    private final Key refreshKey;
    private final RedisService redisService;
    private final CustomUserDetailsService customUserDetailsService;
    private static final Duration ACCESS_TOKEN_EXPIRE_TIME = Duration.ofHours(6);
    private static final Duration REFRESH_TOKEN_EXPIRE_TIME = Duration.ofDays(7);

    public JwtProvider(@Value("${jwt.secret.access}") String accessSecret,
                       @Value("${jwt.secret.refresh}") String refreshSecret,
                       CustomUserDetailsService customUserDetailsService,
                       RedisService redisService) {
        this.accessKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(accessSecret));
        this.refreshKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(refreshSecret));
        this.customUserDetailsService = customUserDetailsService;
        this.redisService = redisService;
    }

    public String generateAccessToken(Long userId, String role) {
        return generateToken(userId, role, accessKey, ACCESS_TOKEN_EXPIRE_TIME);
    }

    // 리프레시 토큰을 redis에 저장
    public String generateRefreshToken(Long userId, String role) {
        String refreshToken = generateToken(userId, role, refreshKey, REFRESH_TOKEN_EXPIRE_TIME);
        String redisKey = "auth:refresh_token:" + userId;
        redisService.setValuesWithTimeout(redisKey, refreshToken, REFRESH_TOKEN_EXPIRE_TIME);
        return refreshToken;
    }

    private String generateToken(Long userId, String role, Key key, Duration expiredTime) {
        Claims claims = Jwts.claims().setSubject(userId.toString());
        claims.put("role", role);

        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expiredTime.toMillis());

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(expireDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public void validateAccessToken(String accessToken) {
        try {
            Jwts.parserBuilder().setSigningKey(accessKey).build().parseClaimsJws(accessToken);
        } catch (JwtException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    public void validateRefreshToken(String refreshToken) {
        try {
            Jwts.parserBuilder().setSigningKey(refreshKey).build().parseClaimsJws(refreshToken);
        } catch (JwtException e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken, accessKey);
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(claims.getSubject());
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    private Claims parseClaims(String token, Key key) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public Claims getRefreshTokenClaims(String refreshToken) {
        return parseClaims(refreshToken, refreshKey);
    }
}