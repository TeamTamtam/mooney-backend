package tamtam.mooney.domain.budget.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record BudgetPlanResponseDto (
        @NotNull
        Long monthlyBudgetAmount,
        List<BudgetPlanRecurringTransactionUnitDto> fixedExpense,
        List<BudgetPlanRecurringTransactionUnitDto> fixedSavings,
        List<CategoryBudgetPlanUnitDto> categoryBudgets
) {}
