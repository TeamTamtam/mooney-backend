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
public class RecurringTransaction extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long recurringTransactionId;

    @NotNull
    private String recurringType; // EXPENSE, INCOME, SAVINGS

    @NotNull
    @Column(nullable = false)
    private String title; // 고정비 항목명

    private long amount;

    private String period; // 고정비 주기

    private LocalDate endDate; // 반복 종료일 (null이면 무한 반복)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public RecurringTransaction(String title, long amount, String period,
                                String recurringType, LocalDate endDate, User user) {
        this.title = title;
        this.amount = amount;
        this.period = period;
        this.recurringType = recurringType;
        this.endDate = endDate;
        this.user = user;
    }

    public void updateAmount(long newAmount) {
        this.amount = (newAmount < 0) ? 0L : newAmount;
    }
}
