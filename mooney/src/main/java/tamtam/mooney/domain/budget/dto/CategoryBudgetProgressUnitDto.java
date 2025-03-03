package tamtam.mooney.domain.budget.dto;

import tamtam.mooney.domain.transaction.entity.ExpenseCategory;

public record CategoryBudgetProgressUnitDto(
        ExpenseCategory expenseCategory,
        long budgetAmount,
        long spentAmount,
        int spentPercentage,
        long remainingAmount
) {}