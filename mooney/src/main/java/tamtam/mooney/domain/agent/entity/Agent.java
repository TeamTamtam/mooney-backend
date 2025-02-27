package tamtam.mooney.domain.agent.entity;

import jakarta.persistence.*;
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

    @NotNull
    private String agentName;

    private String personality;

    private String imgPath;

    @Builder
    public Agent(Long agentId, String agentName, String personality, String imgPath) {
        this.agentId = agentId;
        this.agentName = agentName;
        this.personality = personality;
        this.imgPath = imgPath;
    }
}
