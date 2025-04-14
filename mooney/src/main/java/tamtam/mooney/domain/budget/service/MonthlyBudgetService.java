package tamtam.mooney.domain.budget.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.budget.entity.MonthlyBudget;
import tamtam.mooney.domain.budget.repository.MonthlyBudgetRepository;
import tamtam.mooney.domain.transaction.repository.TransactionRepository;
import tamtam.mooney.domain.transaction.service.ExpenseService;
import tamtam.mooney.domain.user.dto.UserHomeWeeklyBudgetDto;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.global.exception.CustomException;
import tamtam.mooney.global.exception.ErrorCode;

import java.time.LocalDate;

import static java.lang.Math.max;

@Service
@Transactional
@RequiredArgsConstructor
public class MonthlyBudgetService {
    private final MonthlyBudgetRepository monthlyBudgetRepository;
    private final ExpenseService expenseService;
    private final TransactionRepository transactionRepository;

    @Transactional(readOnly = true)
    public MonthlyBudget getMonthlyBudget(User user, LocalDate startOfMonth) {
        return monthlyBudgetRepository.findByUserAndMonthDate(user, startOfMonth)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public UserHomeWeeklyBudgetDto getWeeklyBudgetInfo(User user, LocalDate today) {
        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
        MonthlyBudget monthlyBudget = getMonthlyBudget(user, startOfMonth);

        // 주차 범위 계산 (월~일)
        // 진짜 endOfWeek는 일요일이 아니라 이번 달의 마지막 날을 초과하지 않는 범위
        LocalDate startOfWeek = today.with(java.time.DayOfWeek.MONDAY);
        LocalDate rawEndOfWeek = today.with(java.time.DayOfWeek.SUNDAY);
        LocalDate endOfWeek = rawEndOfWeek.isAfter(endOfMonth) ? endOfMonth : rawEndOfWeek;

        // 이번 주 일 수 / 남은 일 수 = 이번 주가 남은 예산에서 차지하는 비율
        int remainingDays = Math.max(today.lengthOfMonth() - today.getDayOfMonth() + 1, 1);

        // 이번주 지출
        long thisMonthSpentAmount = expenseService.getTotalExpenseAmountForPeriod(user, startOfMonth, endOfMonth);
        // 남은 이번달 예산
        long remainingMonthlyBudget = monthlyBudget.getAmount() - thisMonthSpentAmount;

        // 💡 핵심: 남은 예산 중 이번 주가 차지하는 비율만큼만 예산 할당
        int totalWeekDays = (int) (endOfWeek.toEpochDay() - startOfWeek.toEpochDay() + 1);
        long thisWeekBudgetAmount = remainingMonthlyBudget * totalWeekDays / remainingDays;

        // 사용액 추정 = (이번주 지출 / 오늘까지 경과 일수) * (월요일~오늘 해당하는 일수)
        long thisWeekSpentAmount = expenseService.getTotalExpenseAmountForPeriod(user, startOfWeek, endOfWeek);

        // 예정 지출 추정 = (전체 예정 지출 / 총일수) * (오늘~일요일 일수)
        long scheduledExpenseAmount = 0L; // TODO: 추후 수정

        long thisWeekRemainingBudgetAmount = max(thisWeekBudgetAmount - thisWeekSpentAmount - scheduledExpenseAmount, 0L);
        long dailyBudgetAmount = max((thisWeekRemainingBudgetAmount - scheduledExpenseAmount) / totalWeekDays, 0L);
        int budgetUsagePercentage = max((int)(thisWeekSpentAmount * 100 / thisWeekBudgetAmount), 0);

        return UserHomeWeeklyBudgetDto.builder()
                .remainingBudgetAmount(thisWeekRemainingBudgetAmount) // 이번 주 남은 예산 (오늘~일요일)
                .dailyBudgetAmount(dailyBudgetAmount) // 이번 주 예산 중 하루 예산 (오늘~일요일)
                .budgetUsagePercentage(budgetUsagePercentage) // 예산 퍼센트 (85 등)
                .totalBudgetAmount(thisWeekBudgetAmount) // 이번 주 예산이 원래 얼마였는지
                .spentAmount(thisWeekSpentAmount) // 이번주 예산 중에서 현재까지 사용한 금액 (이번주 월요일~오늘)
                .scheduledExpenseAmount(scheduledExpenseAmount)  // 예정된 지출 (이번주 오늘~일요일 사이)
                .build();
    }
}
