package tamtam.mooney.domain.transaction.dto;

import lombok.Builder;
import tamtam.mooney.domain.enums.ExpenseCategory;
import java.time.LocalDateTime;

@Builder
public record ExpenseUnitResponseDto(
        long transactionId,
        long amount,
        LocalDateTime transactionTime,
        String transactionSource,
        String note,
        String payee,
        ExpenseCategory expenseCategory
) {}
