package tamtam.mooney.domain.transaction.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tamtam.mooney.domain.user.entity.User;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@DiscriminatorValue("EXPENSE") // 구분자 추가 (Joined Table 전략)
public class Expense extends Transaction {

    private String payee;

    @NotNull
    private ExpenseCategory expenseCategory;

    @Builder
    public Expense(String payee, ExpenseCategory expenseCategory,
                   long amount, LocalDateTime transactionTime, String transactionSource, String sourceApp, User user) {
        super(amount, transactionTime, transactionSource, sourceApp, user);
        this.payee = payee;
        this.expenseCategory = expenseCategory;
    }

    public void updateExpenseCategory(ExpenseCategory expenseCategory) {
        this.expenseCategory = expenseCategory;
    }
}
