package tamtam.mooney.domain.agent.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class AgentPrompt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false)
    private Agent agent; // 어느 Agent의 프롬프트인지 연결

    @NotNull
    @Column(nullable = false)
    private String promptType; // 예: "MONTHLY_FEEDBACK", "BUDGET_WARNING"

    @NotNull
    @Column(nullable = false, columnDefinition = "TEXT")
    private String promptText; // 캐릭터의 실제 말투

    @Builder
    public AgentPrompt(Agent agent, String promptType, String promptText) {
        this.agent = agent;
        this.promptType = promptType;
        this.promptText = promptText;
    }

    public void updatePromptText(String promptText) {
        this.promptText = promptText;
    }
}
