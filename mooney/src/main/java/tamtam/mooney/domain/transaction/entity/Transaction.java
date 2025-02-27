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
    @Column(nullable = false)
    private Long amount;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime transactionTime;

    private String transactionSource;

    private String sourceApp;

    @Setter
    private String note;

    @ManyToOne
    @JoinColumn(name = "recurring_transaction_id")
    private RecurringTransaction recurringTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    public Transaction(Long amount, LocalDateTime transactionTime, String transactionSource,
                       String sourceApp, User user) {
        this.amount = amount;
        this.transactionTime = transactionTime;
        this.transactionSource = transactionSource;
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
