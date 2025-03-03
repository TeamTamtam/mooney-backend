package tamtam.mooney.domain.transaction.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.global.common.entity.BaseTimeEntity;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Inheritance(strategy = InheritanceType.JOINED) // 상속 전략 설정
@DiscriminatorColumn(name = "transaction_type") // 구분 컬럼 추가
public abstract class Transaction extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long transactionId;

    private long amount;

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
    @NotNull
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    public Transaction(long amount, LocalDateTime transactionTime, String transactionSource,
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

    public void updateAmount(long newAmount) {
        this.amount = (newAmount < 0) ? 0L : newAmount;
    }
}