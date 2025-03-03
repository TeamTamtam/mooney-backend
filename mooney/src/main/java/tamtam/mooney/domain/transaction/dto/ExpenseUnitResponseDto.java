package tamtam.mooney.domain.transaction.dto;

import lombok.Builder;
import tamtam.mooney.domain.transaction.entity.ExpenseCategory;
import java.time.LocalDateTime;

@Builder
public record ExpenseUnitResponseDto(
        Long transactionId,
        Long amount,
        LocalDateTime transactionTime,
        ExpenseCategory expenseCategory,
        String transactionSource,
        String note
) {}
