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

    @NotBlank
    @Column(nullable = false)
    private String tone;

    private String promptText;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agent_id", nullable = false, updatable = false)
    @NotNull
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Agent agent;

    @Builder
    public UserAgent(String tone, String promptText, User user, Agent agent) {
        this.tone = tone;
        this.promptText = promptText;
        this.user = user;
        this.agent = agent;
    }

    public void updateAmount(String tone) {
        this.tone = tone;
    }

    public void updatePromptText(String promptText) {
        this.promptText = promptText;
    }
}
