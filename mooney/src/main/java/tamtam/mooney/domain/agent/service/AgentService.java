package tamtam.mooney.domain.agent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.agent.entity.Agent;
import tamtam.mooney.domain.agent.repository.AgentRepository;
import tamtam.mooney.global.exception.CustomException;
import tamtam.mooney.global.exception.ErrorCode;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AgentService {

    private final AgentRepository agentRepository;

    public Agent getDefaultAgent() {
        return agentRepository.findByAgentName("무니")
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    public Agent getAgentById(Long agentId) {
        return agentRepository.findById(agentId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
    }
}
