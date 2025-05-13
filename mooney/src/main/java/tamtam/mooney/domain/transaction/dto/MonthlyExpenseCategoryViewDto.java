package tamtam.mooney.domain.transaction.dto;

import tamtam.mooney.domain.enums.ExpenseCategory;

import java.time.LocalDateTime;

public record MonthlyExpenseCategoryViewDto(
        Long transactionId,
        ExpenseCategory category,
        long amount,
        LocalDateTime transactionTime,
        String transactionSource,
        String payee,
        String note
) {}

