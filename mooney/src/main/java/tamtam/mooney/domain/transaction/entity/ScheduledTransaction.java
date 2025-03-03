package tamtam.mooney.domain.transaction.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.global.common.entity.BaseTimeEntity;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class ScheduledTransaction extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long scheduledTransactionId;

    @NotNull
    private String transactionType; // EXPENSE, INCOME, SAVINGS

    @NotNull
    @Column(nullable = false)
    private String title; // 반복 일정 항목명

    @NotNull
    @Column(nullable = false)
    private Long amount;

    @NotNull
    @Column(nullable = false)
    private LocalDate transactionDate; // 실제 발생한 날짜

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public ScheduledTransaction(String transactionType, String title, Long amount, LocalDate transactionDate, User user) {
        this.transactionType = transactionType;
        this.title = title;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.user = user;
    }
}
