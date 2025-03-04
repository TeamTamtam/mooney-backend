package tamtam.mooney.domain.budget.dto;

import tamtam.mooney.domain.enums.ExpenseCategory;

public record CategoryBudgetPlanUnitDto (
        long categoryBudgetId,
        ExpenseCategory expenseCategory,
        long lastMonthExpenseAmount,
        long amount
) {}