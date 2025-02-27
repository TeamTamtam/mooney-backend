package tamtam.mooney.domain.budget.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
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
    private Long amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "monthly_budget_id")
    private MonthlyBudget monthlyBudget;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_category_id")
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
