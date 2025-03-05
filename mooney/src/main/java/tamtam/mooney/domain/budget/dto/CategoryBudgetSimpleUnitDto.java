package tamtam.mooney.domain.budget.dto;

import tamtam.mooney.domain.enums.ExpenseCategory;

public record CategoryBudgetSimpleUnitDto(
        ExpenseCategory expenseCategory,
        long amount
) {}