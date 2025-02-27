package tamtam.mooney.domain.user.entity;
import jakarta.persistence.*;
import lombok.*;
import tamtam.mooney.domain.agent.entity.Agent;
import tamtam.mooney.global.common.entity.BaseTimeEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class UserAgent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long userAgentId;

    private String tone;

    private String promptText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id")
    private Agent agent;

    @Builder
    public UserAgent(String tone, String promptText,
                     User user, Agent agent) {
        this.tone = tone;
        this.promptText = promptText;
        this.user = user;
        this.agent = agent;
    }
}
