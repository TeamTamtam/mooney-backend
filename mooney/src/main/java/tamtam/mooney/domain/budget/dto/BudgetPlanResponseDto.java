package tamtam.mooney.domain.budget.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record BudgetPlanResponseDto (
        Long monthlyBudgetAmount,
        List<BudgetPlanRecurringTransactionUnitDto> fixedExpense,
        List<BudgetPlanRecurringTransactionUnitDto> fixedSavings,
        List<CategoryBudgetPlanUnitDto> categoryBudgets
) {}
