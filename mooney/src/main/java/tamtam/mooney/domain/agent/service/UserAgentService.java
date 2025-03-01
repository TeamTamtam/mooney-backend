package tamtam.mooney.domain.agent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tamtam.mooney.domain.agent.entity.Agent;
import tamtam.mooney.domain.agent.entity.AgentTone;
import tamtam.mooney.domain.agent.entity.UserAgent;
import tamtam.mooney.domain.agent.repository.UserAgentRepository;
import tamtam.mooney.domain.user.entity.User;

@Service
@RequiredArgsConstructor
public class UserAgentService {

    private final UserAgentRepository userAgentRepository;
    private final AgentService agentService;

    public void assignDefaultAgentToUser(User user) {
        Agent defaultAgent = agentService.getDefaultAgent();

        UserAgent userAgent = UserAgent.builder()
                .agentTone(AgentTone.FORMAL)
                .memory(null)
                .user(user)
                .agent(defaultAgent)
                .build();
        userAgentRepository.save(userAgent);
    }
}
