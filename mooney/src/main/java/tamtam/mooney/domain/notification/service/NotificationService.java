package tamtam.mooney.domain.notification.service;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tamtam.mooney.domain.notification.entity.Notification;
import tamtam.mooney.domain.notification.repository.NotificationRepository;
import tamtam.mooney.domain.user.entity.User;

@Service
@Transactional
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public Notification createNotification(String notificationType, String title, String content,
                                           String payload, User user) {
        // 알림 타입 유효성 검증
        if (!NotificationTypeValidator.isValid(notificationType)) {
            throw new IllegalArgumentException("Invalid notification type: " + notificationType);
        }
        // 사용자 정의 제목과 내용으로 Notification 생성
        Notification notification = Notification.builder()
                .notificationType(notificationType)
                .title(title)
                .content(content)
                .payload(payload)
                .isRead(false)
                .user(user)
                .build();
        return notificationRepository.save(notification);
    }

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new IllegalArgumentException("Notification not found"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }
}