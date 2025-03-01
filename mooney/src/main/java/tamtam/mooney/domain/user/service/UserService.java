package tamtam.mooney.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.domain.user.repository.UserRepository;
import tamtam.mooney.global.exception.CustomException;
import tamtam.mooney.global.exception.ErrorCode;

import java.time.ZoneId;

@Service
@Transactional
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    public User createUser(String email, String encryptedPassword, String nickname) {
        User user = User.builder()
                .email(email)
                .encryptedPassword(encryptedPassword)
                .nickname(nickname)
                .timezone(ZoneId.of("Asia/Seoul"))
                .build();
        return userRepository.save(user);
    }

    public void updateRefreshToken(String username, String refreshToken) {
        userRepository.findByEmail(username).ifPresent(user -> {
            user.setRefreshToken(refreshToken);
            userRepository.save(user);
        });
    }

    @Transactional(readOnly = true)
    public String getCurrentUserNickname() {
        return getCurrentUser().getNickname();
    }

    @Transactional(readOnly = true)
    public User getCurrentUser() throws CustomException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_AUTHENTICATED));
    }
}
