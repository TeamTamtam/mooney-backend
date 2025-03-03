package tamtam.mooney.domain.budget.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import tamtam.mooney.domain.transaction.dto.RecurringTransactionDto;

import java.util.List;

public record FirstBudgetRequestDto(
        @Min(2024)
        int year,
        @Min(1) @Max(12)
        int month,
        List<RecurringTransactionDto> fixedIncome,
        List<RecurringTransactionDto> fixedExpense,
        List<RecurringTransactionDto> fixedSavings,
        @NotNull
        Long monthlyBudgetAmount,
        List<CategoryBudgetSimpleUnitDto> categoryBudgets
) {}
