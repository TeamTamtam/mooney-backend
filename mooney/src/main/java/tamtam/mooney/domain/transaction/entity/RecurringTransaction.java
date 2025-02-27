package tamtam.mooney.domain.transaction.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.global.common.entity.BaseTimeEntity;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
public class RecurringTransaction extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long recurringTransactionId;

    @NotNull
    private String type; // EXPENSE, INCOME, SAVINGS

    @NotNull
    private String title; // 고정비 항목명 (예: "월세", "보험료")

    @NotNull
    private Long amount;

    private String period; // 고정비 주기

    @ColumnDefault("false")
    @NotNull
    @Setter
    private Boolean isDeleted;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Builder
    public RecurringTransaction(String type,  String title, Long amount, String period, User user) {
        this.title = title;
        this.amount = amount;
        this.period = period;
        this.type = type;
        this.isDeleted = false;
        this.user = user;
    }
}
