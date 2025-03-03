package tamtam.mooney.domain.user.dto;

import lombok.Builder;

@Builder
public record UserHomeWeeklyBudgetDto(
        Long remainingBudgetAmount,
        Long dailyBudgetAmount,
        int budgetUsagePercentage,
        Long totalBudgetAmount, // 이번 주 예산
        Long spentAmount, // 현재까지 사용한 금액
        Long scheduledExpenseAmount // 예정된 지출
) {}
