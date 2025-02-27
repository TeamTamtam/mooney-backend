package tamtam.mooney.domain.transaction.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tamtam.mooney.domain.user.entity.User;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Expense extends Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long expenseId;

    @NotNull
    private String payee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_category_id")
    private ExpenseCategory expenseCategory;

    @Builder
    public Expense(String payee, ExpenseCategory expenseCategory,
                   Long amount, String transactionSource, LocalDateTime transactionDate, String sourceApp, User user) {
        super(amount, transactionSource, transactionDate, sourceApp, user);
        this.payee = payee;
        this.expenseCategory = expenseCategory;
    }

    public void updateExpenseCategory(ExpenseCategory expenseCategory) {
        this.expenseCategory = expenseCategory;
    }
}
