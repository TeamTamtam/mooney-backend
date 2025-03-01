package tamtam.mooney.global.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import tamtam.mooney.global.exception.CustomException;
import tamtam.mooney.global.exception.ErrorCode;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final CookieUtil cookieUtil;
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader(AUTHORIZATION_HEADER);

        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            String accessToken = authorizationHeader.substring(BEARER_PREFIX.length());
            try {
                jwtProvider.validateAccessToken(accessToken); // 엑세스 토큰 유효성을 검증
                Authentication authentication = jwtProvider.getAuthentication(accessToken);
                SecurityContextHolder.getContext().setAuthentication(authentication); // 인증된 사용자 정보를 SecurityContext에 저장
            } catch (CustomException e) {
                SecurityContextHolder.clearContext();
                if (e.getErrorCode().equals(ErrorCode.EXPIRED_ACCESS_TOKEN)) {
                    // 리프레시 토큰 검증을 위해 쿠키에서 리프레시 토큰 가져오기
                    String refreshToken = cookieUtil.getCookieValue(request, "refreshToken");
                    if (refreshToken != null) {
                        try {
                            // 리프레시 토큰의 유효성을 검증
                            jwtProvider.validateRefreshToken(refreshToken);
                            ErrorResponseUtil.writeErrorResponse(response, e.getErrorCode(), request.getRequestURI());
                            return;
                        } catch (CustomException ex) {
                            ErrorResponseUtil.writeErrorResponse(response, ex.getErrorCode(), request.getRequestURI());
                            return;
                        }
                    } else {
                        ErrorResponseUtil.writeErrorResponse(response, ErrorCode.NO_COOKIE, request.getRequestURI());
                        return;
                    }
                }
            }
        }
        filterChain.doFilter(request,response);  // 다음 필터로 요청을 넘김
    }
}

