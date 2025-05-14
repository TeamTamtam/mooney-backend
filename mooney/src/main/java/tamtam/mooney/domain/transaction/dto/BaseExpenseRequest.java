package tamtam.mooney.domain.transaction.dto;

import java.time.LocalDateTime;

public interface BaseExpenseRequest {
    String payee();
    long amount();
    LocalDateTime transactionTime();
    String transactionSource();
    String sourceApp();
}
