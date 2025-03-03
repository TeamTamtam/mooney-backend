package tamtam.mooney.global.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.agent.entity.Agent;
import tamtam.mooney.domain.agent.repository.AgentRepository;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AgentInitializer {

    private final AgentRepository agentRepository;

    @PostConstruct
    @Transactional
    public void initAgents() {
        if (agentRepository.count() > 0) return; // 이미 데이터가 있으면 초기화 X

        Set.of(
                "무니, 정석적이고 친절한 가계부 전문가, /png",
                "써니, 활발하고 쾌활한 응원러, /png",
                "티타, 깐깐하고 엄격한 절약 전문가, /png",
                "에피, 감성적이고 위로를 잘하는 힐링 캐릭터, /png"
        ).forEach(agent -> {
            String[] parts = agent.split(",");
            agentRepository.save(new Agent(parts[0].trim(), parts[1].trim(), parts[2].trim()));
            // System.out.println("✅ Saved Agent: " + parts[0] + " (" + parts[1] + ")");
        });

        System.out.println("🚀 Agents initialized!");
    }
}
