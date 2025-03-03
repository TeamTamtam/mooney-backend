package tamtam.mooney.domain.budget.dto;

public record CategoryBudgetProgressUnitDto(
        String icon,
        String categoryName,
        Long amount, // 예산
        Long spent, // 지출
        int spentPercentage, // 지출 퍼센트
        Long remaining // 남은 금액
) {}