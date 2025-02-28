package tamtam.mooney.domain.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.domain.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
