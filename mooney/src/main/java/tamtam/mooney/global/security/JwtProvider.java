package tamtam.mooney.global.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tamtam.mooney.global.exception.CustomException;
import tamtam.mooney.global.exception.ErrorCode;

import java.time.Duration;
import java.util.Date;

@Component
public class JwtProvider {
    @Value("${jwt.secret.access}")
    private String SECRET_KEY;

    private final Duration ACCESS_TOKEN_EXPIRY = Duration.ofDays(1);
    private final Duration REFRESH_TOKEN_EXPIRY = Duration.ofDays(31);

    public String createAccessToken(Long userId) {
        return createToken(userId, ACCESS_TOKEN_EXPIRY);
    }

    public String createRefreshToken(Long userId) {
        return createToken(userId, REFRESH_TOKEN_EXPIRY);
    }

    private String createToken(Long userId, Duration expiryDuration) {
        Date now = new Date();
        return Jwts.builder()
                .setSubject(String.valueOf(userId))
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiryDuration.toMillis()))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public Long validateToken(String token) {
        try {
            return Long.valueOf(Jwts.parser()
                    .setSigningKey(SECRET_KEY)
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject());
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }
}
