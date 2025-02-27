package tamtam.mooney.domain.agent.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tamtam.mooney.global.common.entity.BaseTimeEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Agent extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long agentId;

    @NotBlank
    @Column(nullable = false)
    private String agentName;

    @NotBlank
    @Column(nullable = false)
    private String personality;

    private String promptText;

    @NotBlank
    @Column(nullable = false)
    private String imgPath;

    @Builder
    public Agent(String agentName, String personality, String promptText, String imgPath) {
        this.agentName = agentName;
        this.personality = personality;
        this.promptText = promptText;
        this.imgPath = imgPath;
    }

    public void updatePersonality(String personality) {
        this.personality = personality;
    }

    public void updatePromptText(String promptText) {
        this.promptText = promptText;
    }
}