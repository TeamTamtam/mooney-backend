package tamtam.mooney.domain.transaction.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tamtam.mooney.domain.user.entity.User;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class Income extends Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long incomeId;

    @NotNull
    private String payer;

    @NotNull
    private IncomeCategory incomeCategory;

    @Builder
    public Income(String payer, IncomeCategory incomeCategory,
                  Long amount, String transactionSource, LocalDateTime transactionDate, String sourceApp, User user) {
        super(amount, transactionSource, transactionDate, sourceApp, user);
        this.payer = payer;
        this.incomeCategory = incomeCategory;
    }

    public void updateIncomeCategory(IncomeCategory incomeCategory) {
        this.incomeCategory = incomeCategory;
    }
}
