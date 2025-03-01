package tamtam.mooney.domain.transaction.dto;

import jakarta.validation.constraints.NotNull;
import tamtam.mooney.global.exception.CustomException;
import tamtam.mooney.global.exception.ErrorCode;

import java.time.LocalDateTime;

public record ExpenseAddRequestDto(
        @NotNull(message = "Amount is required")
        Long amount,
        @NotNull(message = "Transaction time is required")
        LocalDateTime transactionTime,

        String transactionSource,
        String sourceApp,

        String payee,
        String paymentMethod
) {
    public ExpenseAddRequestDto {
        // 금액 검증 (0 이상이어야 함)
        if (amount != null && amount < 0) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }
    }
}
