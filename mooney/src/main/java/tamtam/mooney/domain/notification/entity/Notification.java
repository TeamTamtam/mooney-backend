package tamtam.mooney.domain.notification.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
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
    @Column(nullable = false)
    private String notificationType;

    @NotNull
    @Column(nullable = false)
    private String title;

    @NotNull
    @Column(nullable = false)
    private String content;

    // JSON 형태로 추가 데이터 저장 (예: 링크, 이미지, 기타 옵션)
    private String payload;

    @NotNull
    @Column(nullable = false)
    private Boolean isRead;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @NotNull
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Builder
    public Notification(String notificationType, String title, String content,
                        String payload, Boolean isRead, User user) {
        this.notificationType = notificationType;
        this.title = title;
        this.content = content;
        this.payload = payload;
        this.isRead = isRead;
        this.user = user;
        this.createdAt = LocalDateTime.now();
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }
}
