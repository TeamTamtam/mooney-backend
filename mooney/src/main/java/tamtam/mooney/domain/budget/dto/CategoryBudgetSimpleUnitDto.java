package tamtam.mooney.domain.budget.dto;

import tamtam.mooney.domain.transaction.entity.ExpenseCategory;

public record CategoryBudgetSimpleUnitDto(
        ExpenseCategory expenseCategory,
        long amount
) {}