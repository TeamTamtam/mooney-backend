package tamtam.mooney.domain.budget.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.global.common.entity.BaseTimeEntity;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class MonthlyBudget extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long monthlyBudgetId;

    // 예: 2023-03-01 형태로 해당 월을 표현
    private LocalDate monthDate;

    @NotNull
    private Long amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public MonthlyBudget(Long monthlyBudgetId, User user, LocalDate monthDate, Long amount) {
        this.monthlyBudgetId = monthlyBudgetId;
        this.user = user;
        this.monthDate = monthDate;
        this.amount = amount;
    }

    public void updateAmount(Long newAmount) {
        if (newAmount == null || newAmount < 0) {
            this.amount = 0L;
        } else {
            this.amount = newAmount;
        }
    }
}
