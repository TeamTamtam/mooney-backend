package tamtam.mooney.domain.transaction.entity;

import jakarta.persistence.*;
import lombok.*;
import tamtam.mooney.domain.user.entity.User;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@DiscriminatorValue("INCOME") // 구분자 추가 (Joined Table 전략)
public class Income extends Transaction {

    private String payer;

    @Builder
    public Income(String payer, long amount, LocalDateTime transactionTime,
                  String transactionSource, String sourceApp, User user) {
        super(amount, transactionTime, transactionSource, sourceApp, user);
        this.payer = payer;
    }
}