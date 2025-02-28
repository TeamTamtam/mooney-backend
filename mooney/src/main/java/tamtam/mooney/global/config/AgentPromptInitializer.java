package tamtam.mooney.global.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.agent.entity.Agent;
import tamtam.mooney.domain.agent.entity.AgentPrompt;
import tamtam.mooney.domain.agent.repository.AgentPromptRepository;
import tamtam.mooney.domain.agent.repository.AgentRepository;

import java.util.List;
import java.util.Map;

@Service
@DependsOn("agentInitializer")
@RequiredArgsConstructor
public class AgentPromptInitializer {

    private final AgentRepository agentRepository;
    private final AgentPromptRepository agentPromptRepository;

    @PostConstruct
    @Transactional
    public void initAgentPrompts() {
        if (agentPromptRepository.count() > 0) return; // 이미 데이터가 있으면 초기화 X

        // 캐릭터별 프롬프트 맵
        Map<String, List<String>> prompts = Map.of(
                "무니", List.of(
                        "MONTHLY_FEEDBACK, 이번 달 지출을 꼼꼼히 분석해봤어요. 🍀 절약할 수 있는 부분을 체크해볼까요?",
                        "BUDGET_WARNING, ⚠️ 이번 달 예산을 초과했어요. 다음 달 계획을 다시 세워볼까요?"
                ),
                "써니", List.of(
                        "MONTHLY_FEEDBACK, 와! 이번 달도 멋지게 소비했네요! 🎉 그래도 저축도 조금 해두는 게 어때요?",
                        "BUDGET_WARNING, 괜찮아요! 🎈 다음 달에는 조금 더 신중하게 써보는 것도 좋을 것 같아요!"
                ),
                "티타", List.of(
                        "MONTHLY_FEEDBACK, 이건 낭비입니다. 불필요한 소비를 줄이지 않으면 미래가 힘들어질 수 있어요.",
                        "BUDGET_WARNING, 이건 심각합니다. 지출 패턴을 바로 수정해야 해요. 💢"
                ),
                "에피", List.of(
                        "MONTHLY_FEEDBACK, 이번 달도 고생 많았어요. 💕 소비가 조금 많아도, 행복을 위한 소비라면 괜찮아요!",
                        "BUDGET_WARNING, 요즘 힘든 일 있었나요? 💖 나 자신을 위한 소비도 필요하죠. 하지만 균형도 중요해요!"
                )
        );

        // Agent 엔티티 가져오기
        prompts.forEach((agentName, agentPrompts) -> {
            Agent agent = agentRepository.findByAgentName(agentName)
                    .orElseThrow(() -> new RuntimeException("Agent not found: " + agentName));

            agentPrompts.forEach(prompt -> {
                String[] parts = prompt.split(",", 2);
                agentPromptRepository.save(new AgentPrompt(agent, parts[0].trim(), parts[1].trim()));
            });
        });

        System.out.println("Agent Prompts initialized!");
    }
}
