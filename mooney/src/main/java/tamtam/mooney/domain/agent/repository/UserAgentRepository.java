package tamtam.mooney.domain.agent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.domain.agent.entity.UserAgent;
import tamtam.mooney.domain.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserAgentRepository extends JpaRepository<UserAgent, Long> {
    Optional<UserAgent> findByUserAndIsActiveTrue(User user);
    List<UserAgent> findByUser(User user);
}
