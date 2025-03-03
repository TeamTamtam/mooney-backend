package tamtam.mooney.domain.budget.dto;

public record CategoryBudgetProgressUnitDto(
        String icon,
        String categoryName,
        long amount,
        long spent,
        int spentPercentage,
        long remaining
) {}