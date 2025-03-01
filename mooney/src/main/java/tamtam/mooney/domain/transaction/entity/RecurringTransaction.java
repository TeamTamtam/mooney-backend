package tamtam.mooney.domain.transaction.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.global.common.entity.BaseTimeEntity;

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

    @NotNull
    @Column(nullable = false)
    private Long amount;

    private String period; // 고정비 주기

    @ColumnDefault("false")
    @NotNull
    @Column(nullable = false)
    @Setter
    private Boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public RecurringTransaction(String recurringType,  String title, Long amount, String period, User user) {
        this.title = title;
        this.amount = amount;
        this.period = period;
        this.recurringType = recurringType;
        this.isDeleted = false;
        this.user = user;
    }
}
