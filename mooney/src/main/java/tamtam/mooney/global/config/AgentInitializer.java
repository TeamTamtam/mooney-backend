package tamtam.mooney.global.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.agent.entity.Agent;
import tamtam.mooney.domain.agent.repository.AgentRepository;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AgentInitializer {
    private final AgentRepository agentRepository;
    private static final String DEFAULT_IMG = "/img/default.png";

    @PostConstruct
    @Transactional
    public void initAgents() {
        if (agentRepository.count() > 0) return; // 이미 데이터가 있으면 초기화 X
        List.of(
                "무니, 정석적이고 친절한 가계부 전문가",
                "써니, 활발하고 쾌활한 치어리더",
                "티타, 깐깐하고 엄격한 절약 전문가",
                "에피, 감성적이고 위로를 잘하는 힐링 캐릭터"
        ).forEach(s -> {
            String[] parts = s.split("\\s*,\\s*", 2);
            String name = parts[0].strip();
            String personality = (parts.length > 1) ? parts[1].strip() : "";
            agentRepository.save(new Agent(name, personality, DEFAULT_IMG));
        });
    }
}
