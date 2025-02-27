package tamtam.mooney.domain.notification.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import tamtam.mooney.domain.user.entity.User;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long notificationId;

    @NotNull
    private String notificationType;

    @NotNull
    private String title;

    @NotNull
    private String content;

    // JSON 형태로 추가 데이터 저장 (예: 링크, 이미지, 기타 옵션)
    @Column(columnDefinition = "TEXT")
    private String payload;

    @ColumnDefault("false")
    @NotNull
    private Boolean isRead;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Notification(String title, String content, String notificationType,
                        String payload, Boolean isRead, User user) {
        this.title = title;
        this.content = content;
        setType(notificationType);
        this.payload = payload;
        this.isRead = isRead;
        this.user = user;
    }

    public void setType(String notificationType) {
        if (!NotificationTypeValidator.isValid(notificationType)) {
            throw new IllegalArgumentException("Invalid notification type: " + notificationType);
        }
        this.notificationType = notificationType;
    }
}
