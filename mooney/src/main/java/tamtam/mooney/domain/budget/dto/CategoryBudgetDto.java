package tamtam.mooney.domain.budget.dto;

import jakarta.validation.constraints.NotNull;
import tamtam.mooney.domain.transaction.entity.ExpenseCategory;

public record CategoryBudgetDto(
        @NotNull
        ExpenseCategory expenseCategory,
        @NotNull
        Long amount
) {}