package tamtam.mooney.domain.budget.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record FirstBudgetRequestDto(
        List<RecurringTransactionDto> fixedIncome,
        List<RecurringTransactionDto> fixedExpense,
        List<RecurringTransactionDto> fixedSavings,
        @NotNull
        LocalDate monthDate,
        @NotNull
        Long monthlyBudgetAmount,
        List<CategoryBudgetDto> categoryBudgets
) {}
