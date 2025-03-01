package tamtam.mooney.domain.user.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tamtam.mooney.domain.user.service.UserService;


@Tag(name = "User")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    @Operation(summary = "유저 닉네임 조회")
    @GetMapping("/nickname")
    public ResponseEntity<?> getUserInfo() {
        return ResponseEntity.ok().body(userService.getCurrentUserNickname());
    }

    @Operation(summary = "설정에서 유저 정보 조회")
    @GetMapping("/settings")
    public ResponseEntity<?> getUserSettingsInfo() {
        return ResponseEntity.ok().body(userService.getUserSettingsInfo());
    }
}