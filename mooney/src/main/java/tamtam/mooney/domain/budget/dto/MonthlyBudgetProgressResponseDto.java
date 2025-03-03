package tamtam.mooney.domain.budget.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record MonthlyBudgetProgressResponseDto(
        Long remainingBudget,
        Long dailyBudget,
        Long monthlyBudget,
        Long recurringExpense,
        Long totalExpense,
        List<CategoryBudgetProgressUnitDto> categoryBudgets
) {}