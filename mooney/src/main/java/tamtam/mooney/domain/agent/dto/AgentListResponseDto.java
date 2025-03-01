package tamtam.mooney.domain.agent.dto;

public record AgentListResponseDto(
        Long agentId,
        String agentName,
        String imgPath,
        String personality,
        boolean isUnlocked, // 사용자가 보유한 Agent인지 여부
        Long userAgentId // 보유한 경우 userAgentId 포함 (null 가능)
) {}
