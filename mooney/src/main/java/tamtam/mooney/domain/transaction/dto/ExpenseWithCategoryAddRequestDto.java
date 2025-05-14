package tamtam.mooney.domain.transaction.dto;

import jakarta.validation.constraints.NotNull;
import tamtam.mooney.domain.enums.ExpenseCategory;

import java.time.LocalDateTime;

public record ExpenseWithCategoryAddRequestDto(
        long amount,
        @NotNull(message = "Transaction time is required")
        LocalDateTime transactionTime,
        String transactionSource,
        String sourceApp,
        @NotNull(message = "Payee is required")
        String payee,
        ExpenseCategory expenseCategory
) implements BaseExpenseRequest {
}