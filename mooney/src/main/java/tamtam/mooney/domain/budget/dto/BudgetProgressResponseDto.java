package tamtam.mooney.domain.budget.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record BudgetProgressResponseDto(
        Long remainingBudgetAmount,
        Long dailyBudgetAmount,
        Long monthlyBudgetAmount,
        Long pendingExpenseAmount,
        Long totalExpenseAmount,
        List<CategoryBudgetProgressUnitDto> categoryBudgets
) {}