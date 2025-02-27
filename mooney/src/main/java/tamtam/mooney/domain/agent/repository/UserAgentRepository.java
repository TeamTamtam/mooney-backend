package tamtam.mooney.domain.agent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.domain.agent.entity.UserAgent;

public interface UserAgentRepository extends JpaRepository<UserAgent, Long> {
}
