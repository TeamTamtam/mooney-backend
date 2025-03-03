package tamtam.mooney.domain.auth.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tamtam.mooney.domain.auth.dto.AuthSignUpRequestDto;
import tamtam.mooney.domain.auth.dto.AuthLoginRequestDto;
import tamtam.mooney.domain.auth.dto.TokenResponseDto;
import tamtam.mooney.domain.auth.service.AuthService;

@Tag(name = "Auth")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "이메일 중복 확인")
    @GetMapping("/check-email")
    public ResponseEntity<String> checkEmailAvailability(@RequestParam @NotNull String email) {
        authService.validateEmailAvailability(email);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ResponseEntity<TokenResponseDto> signUp(@RequestBody @Valid AuthSignUpRequestDto requestDto) {
        return ResponseEntity.ok(authService.signUp(requestDto));
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<TokenResponseDto> login(@RequestBody @Valid AuthLoginRequestDto requestDto) {
        return ResponseEntity.ok(authService.login(requestDto));
    }

    @Operation(summary = "액세스 토큰 재발급")
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponseDto> refreshAccessToken(@RequestParam @NotNull String refreshToken) {
        return ResponseEntity.ok(authService.refreshAccessToken(refreshToken));
    }
}