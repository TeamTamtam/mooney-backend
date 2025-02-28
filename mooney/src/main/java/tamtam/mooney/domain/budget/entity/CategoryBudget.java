package tamtam.mooney.domain.budget.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import tamtam.mooney.domain.transaction.entity.ExpenseCategory;
import tamtam.mooney.global.common.entity.BaseTimeEntity;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class CategoryBudget extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long categoryBudgetId;

    @NotNull
    @Column(nullable = false)
    private Long amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monthly_budget_id", nullable = false, updatable = false)
    @NotNull
    @OnDelete(action = OnDeleteAction.CASCADE)
    private MonthlyBudget monthlyBudget;

    @NotNull
    private ExpenseCategory expenseCategory;

    @Builder
    public CategoryBudget(MonthlyBudget monthlyBudget, ExpenseCategory expenseCategory, Long amount) {
        this.monthlyBudget = monthlyBudget;
        this.expenseCategory = expenseCategory;
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
