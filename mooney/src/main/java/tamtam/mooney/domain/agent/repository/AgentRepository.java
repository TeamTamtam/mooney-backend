package tamtam.mooney.domain.agent.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.domain.agent.entity.Agent;

import java.util.Optional;


public interface AgentRepository extends JpaRepository<Agent, Long> {
    Optional<Agent> findByAgentName(String agentName);
}
