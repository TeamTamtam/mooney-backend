package tamtam.mooney.domain.agent.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.agent.dto.AgentListResponseDto;
import tamtam.mooney.domain.agent.dto.AgentResponseDto;
import tamtam.mooney.domain.agent.entity.Agent;
import tamtam.mooney.domain.agent.entity.AgentTone;
import tamtam.mooney.domain.agent.entity.UserAgent;
import tamtam.mooney.domain.agent.repository.AgentRepository;
import tamtam.mooney.domain.agent.repository.UserAgentRepository;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.domain.user.service.UserService;
import tamtam.mooney.global.exception.CustomException;
import tamtam.mooney.global.exception.ErrorCode;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserAgentService {

    private final UserAgentRepository userAgentRepository;
    private final UserService userService;
    private final AgentService agentService;
    private final AgentRepository agentRepository;

    public void assignDefaultAgentToUser(User user) {
        Agent defaultAgent = agentService.getDefaultAgent();

        UserAgent userAgent = UserAgent.builder()
                .user(user)
                .agent(defaultAgent)
                .agentTone(AgentTone.FORMAL)
                .isActive(true)
                .build();
        userAgentRepository.save(userAgent);
    }

    public void createNewUserAgent(Long agentId) {
        User user = userService.getCurrentUser();
        Agent agent = agentService.getAgentById(agentId);

        UserAgent userAgent = UserAgent.builder()
                .user(user)
                .agent(agent)
                .agentTone(AgentTone.FORMAL)
                .isActive(false)
                .build();
        userAgentRepository.save(userAgent);
    }

    @Transactional(readOnly = true)
    public AgentResponseDto getActiveUserAgentInfo() {
        User user = userService.getCurrentUser();
        UserAgent activeUserAgent = getActiveUserAgent(user);
        return new AgentResponseDto(
                activeUserAgent.getAgent().getAgentName(),
                activeUserAgent.getAgent().getImgPath()
        );
    }

    @Transactional(readOnly = true)
    public UserAgent getActiveUserAgent(User user) {
        return userAgentRepository.findByUserAndIsActiveTrue(user)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    public void activateUserAgent(Long userAgentId) {
        User user = userService.getCurrentUser();
        // 해당 Agent를 보유하고 있는지 확인
        UserAgent targetUserAgent = userAgentRepository.findById(userAgentId)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));

        // 기존 활성화된 Agent 비활성화
        userAgentRepository.findByUserAndIsActiveTrue(user)
                .ifPresent(activeUserAgent -> {
                    activeUserAgent.setActive(false);
                    userAgentRepository.save(activeUserAgent);
                });

        // 선택한 Agent 활성화
        targetUserAgent.setActive(true);
        userAgentRepository.save(targetUserAgent);
    }

    public List<AgentListResponseDto> getAllAgentsWithUnlockStatus() {
        User user = userService.getCurrentUser();
        return agentRepository.getAllAgentsWithUnlockStatus(user);
    }
}