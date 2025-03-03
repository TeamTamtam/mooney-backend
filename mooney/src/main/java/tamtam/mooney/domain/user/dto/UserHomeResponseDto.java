package tamtam.mooney.domain.user.dto;

import lombok.Builder;
import tamtam.mooney.domain.transaction.dto.ExpenseUnitResponseDto;

import java.util.List;

@Builder
public record UserHomeResponseDto(
        String welcomeMessage,
        String agentImgPath,
        List<UserHomeWeeklyMissionDto> weeklyMissions,
        Long todayExpenseAmount,
        List<ExpenseUnitResponseDto> recentExpenses,
        UserHomeWeeklyBudgetDto weeklyBudget
) {}