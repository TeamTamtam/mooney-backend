package tamtam.mooney.global.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Component;
import tamtam.mooney.domain.user.repository.UserRepository;
import tamtam.mooney.global.exception.ErrorCode;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomLogoutHandler implements LogoutHandler {
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final CookieUtil cookieUtil;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if (authentication != null && authentication.getName() != null) {
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String accessToken = authHeader.substring(7);
                jwtProvider.validateAccessToken(accessToken);

                // User 엔티티에서 refreshToken 제거
                userRepository.findByEmail(authentication.getName()).ifPresent(user -> {
                    user.setRefreshToken(null);
                    userRepository.save(user);
                });
            }
            // 쿠키 삭제
            cookieUtil.deleteCookie(response, "refreshToken");

            // 기본 로그아웃 핸들러 수행
            SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();
            logoutHandler.logout(request, response, authentication);
        } else {
            ErrorResponseUtil.writeErrorResponse(response, ErrorCode.NOT_AUTHENTICATED, request.getRequestURI());
        }
    }
}
