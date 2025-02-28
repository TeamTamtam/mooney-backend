package tamtam.mooney.domain.agent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.agent.repository.AgentRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class AgentService {
    private final AgentRepository agentRepository;
}
