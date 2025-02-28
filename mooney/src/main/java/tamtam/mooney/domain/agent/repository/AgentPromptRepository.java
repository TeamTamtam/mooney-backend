package tamtam.mooney.domain.agent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.domain.agent.entity.AgentPrompt;

public interface AgentPromptRepository extends JpaRepository<AgentPrompt, Long> {
}

