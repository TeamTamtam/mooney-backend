package tamtam.mooney.domain.budget.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import tamtam.mooney.domain.agent.entity.UserAgent;
import tamtam.mooney.global.common.entity.BaseTimeEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class MonthlyComment extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long monthlyCommentId;

    @NotBlank
    @Lob
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 코멘트 내용

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monthly_budget_id", nullable = false, updatable = false)
    @NotNull
    @OnDelete(action = OnDeleteAction.CASCADE)
    private MonthlyBudget monthlyBudget;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_agent_id", nullable = false)
    private UserAgent userAgent;

    @Builder
    public MonthlyComment(String content, MonthlyBudget monthlyBudget, UserAgent userAgent) {
        this.content = content;
        this.monthlyBudget = monthlyBudget;
        this.userAgent = userAgent;
    }
}
