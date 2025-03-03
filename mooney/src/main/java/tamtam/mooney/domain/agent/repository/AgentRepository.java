package tamtam.mooney.domain.agent.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tamtam.mooney.domain.agent.dto.AgentListResponseDto;
import tamtam.mooney.domain.agent.entity.Agent;
import tamtam.mooney.domain.user.entity.User;

import java.util.List;
import java.util.Optional;


public interface AgentRepository extends JpaRepository<Agent, Long> {
    Optional<Agent> findByAgentName(String agentName);

    @Query("""
    SELECT new tamtam.mooney.domain.agent.dto.AgentListResponseDto(
        a.agentId,
        a.agentName,
        a.imgPath,
        a.personality,
        CASE WHEN ua.userAgentId IS NOT NULL THEN TRUE ELSE FALSE END,
        ua.userAgentId
    )
    FROM Agent a
    LEFT JOIN UserAgent ua ON ua.agent.agentId = a.agentId AND ua.user = :user
""")
    List<AgentListResponseDto> getAllAgentsWithUnlockStatus(@Param("user") User user);

}
