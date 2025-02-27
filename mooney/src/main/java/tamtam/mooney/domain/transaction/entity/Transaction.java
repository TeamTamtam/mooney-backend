package tamtam.mooney.domain.transaction.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.global.common.entity.BaseTimeEntity;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@MappedSuperclass
public abstract class Transaction extends BaseTimeEntity {

    @NotNull
    private Long amount;

    @NotNull
    private String transactionSource;

    @NotNull
    private LocalDateTime transactionDate;

    private String sourceApp;

    @Setter
    private String note; // TODO: String 입력받는 것들에 글자 제한 걸기

    @ManyToOne
    @JoinColumn(name = "recurring_transaction_id")
    private RecurringTransaction recurringTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Transaction(Long amount, String transactionSource, LocalDateTime transactionDate,
                       String sourceApp, User user) {
        this.amount = amount;
        this.transactionSource = transactionSource;
        this.transactionDate = transactionDate;
        this.sourceApp = sourceApp;
        this.user = user;
    }

    public void setRecurring(RecurringTransaction recurringTransaction) {
        this.recurringTransaction = recurringTransaction;
    }

    public void updateAmount(Long newAmount) {
        if (newAmount == null || newAmount < 0) {
            this.amount = 0L;
        } else {
            this.amount = newAmount;
        }
    }
}
