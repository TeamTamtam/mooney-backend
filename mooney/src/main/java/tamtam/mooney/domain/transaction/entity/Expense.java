package tamtam.mooney.domain.transaction.entity;

import jakarta.persistence.*;
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

    private String payee;

    private String paymentMethod;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_category_id")
    private ExpenseCategory expenseCategory;

    @Builder
    public Expense(String payee, String paymentMethod, ExpenseCategory expenseCategory,
                   Long amount, LocalDateTime transactionDate, String transactionSource, String sourceApp, User user) {
        super(amount, transactionDate, transactionSource, sourceApp, user);
        this.payee = payee;
        this.paymentMethod = paymentMethod;
        this.expenseCategory = expenseCategory;
    }

    public void updateExpenseCategory(ExpenseCategory expenseCategory) {
        this.expenseCategory = expenseCategory;
    }
}
