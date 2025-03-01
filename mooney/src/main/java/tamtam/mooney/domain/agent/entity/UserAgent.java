package tamtam.mooney.domain.agent.entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.global.common.entity.BaseTimeEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class UserAgent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long userAgentId;

    @Enumerated(EnumType.STRING)
    @NotNull
    @Column(nullable = false)
    private AgentTone agentTone;

    private String memory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    @NotNull
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false, updatable = false)
    @NotNull
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Agent agent;

    @Builder
    public UserAgent(AgentTone agentTone, String memory, User user, Agent agent) {
        this.agentTone = agentTone;
        this.memory = memory;
        this.user = user;
        this.agent = agent;
    }

    public void updateTone(AgentTone agentTone) {
        this.agentTone = agentTone;
    }

    public void updateMemory(String memory) {
        this.memory = memory;
    }
}
