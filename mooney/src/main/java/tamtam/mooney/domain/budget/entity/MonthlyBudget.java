package tamtam.mooney.domain.budget.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
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

    @NotNull
    @Column(nullable = false)
    private LocalDate monthDate; // 예: 2023-03-01

    @NotNull
    @Column(nullable = false)
    private long amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    @NotNull
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @Builder
    public MonthlyBudget(Long monthlyBudgetId, User user, LocalDate monthDate, long amount) {
        this.monthlyBudgetId = monthlyBudgetId;
        this.user = user;
        this.monthDate = monthDate;
        this.amount = amount;
    }

    public void updateAmount(long newAmount) {
        this.amount = (newAmount < 0) ? 0L : newAmount;
    }
}
