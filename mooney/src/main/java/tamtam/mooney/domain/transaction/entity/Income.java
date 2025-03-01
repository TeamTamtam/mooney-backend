package tamtam.mooney.domain.transaction.entity;

import jakarta.persistence.*;
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

    private String payer;

    // private String incomeCategory;

    @Builder
    public Income(String payer, Long amount, LocalDateTime transactionDate, String transactionSource, String sourceApp, User user) {
        super(amount, transactionDate, transactionSource, sourceApp, user);
        this.payer = payer;
    }
}
