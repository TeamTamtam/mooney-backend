package tamtam.mooney.domain.budget.dto;

import tamtam.mooney.domain.enums.ExpenseCategory;

public record CategoryBudgetProgressUnitDto(
        ExpenseCategory expenseCategory,
        long budgetAmount,
        long spentAmount,
        int spentPercentage,
        long remainingAmount
) {}