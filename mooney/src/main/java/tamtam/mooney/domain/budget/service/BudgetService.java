package tamtam.mooney.domain.budget.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.budget.dto.BudgetModifyRequestDto;
import tamtam.mooney.domain.budget.dto.BudgetProgressResponseDto;
import tamtam.mooney.domain.budget.dto.CategoryBudgetProgressUnitDto;
import tamtam.mooney.domain.budget.dto.FirstBudgetRequestDto;
import tamtam.mooney.domain.budget.entity.MonthlyBudget;
import tamtam.mooney.domain.budget.repository.MonthlyBudgetRepository;
import tamtam.mooney.domain.transaction.dto.RecurringTransactionDto;
import tamtam.mooney.domain.transaction.service.ExpenseService;
import tamtam.mooney.domain.transaction.service.RecurringTransactionService;
import tamtam.mooney.domain.transaction.service.ScheduledTransactionService;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.domain.user.service.UserService;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class BudgetService {
    private final MonthlyBudgetRepository monthlyBudgetRepository;
    private final UserService userService;
    private final RecurringTransactionService recurringTransactionService;
    private final ScheduledTransactionService scheduledTransactionService;
    private final ExpenseService expenseService;
    private final MonthlyBudgetService monthlyBudgetService;
    private final CategoryBudgetService categoryBudgetService;

    // 첫 예산 수립
    public void saveFirstBudget(FirstBudgetRequestDto requestDto) {
        User user = userService.getCurrentUser();
        // 월 예산 저장
        MonthlyBudget monthlyBudget = MonthlyBudget.builder().user(user).monthDate(requestDto.monthDate()).amount(requestDto.monthlyBudgetAmount()).build();
        monthlyBudgetRepository.save(monthlyBudget);
        // 고정 수입, 지출, 저축 저장
        recurringTransactionService.saveRecurringTransactions(user, requestDto.fixedIncome(), "INCOME");
        recurringTransactionService.saveRecurringTransactions(user, requestDto.fixedExpense(), "EXPENSE");
        recurringTransactionService.saveRecurringTransactions(user, requestDto.fixedSavings(), "SAVINGS");
        // 카테고리별 예산 저장
        categoryBudgetService.saveCategoryBudgets(monthlyBudget, requestDto.categoryBudgets());
    }

    @Transactional(readOnly = true)
    public BudgetProgressResponseDto getBudgetProgress(int year, int month, LocalDate today) {
        User user = userService.getCurrentUser();
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());
        // 월 예산 총액
        MonthlyBudget monthlyBudget = monthlyBudgetService.getMonthlyBudget(user, startOfMonth);
        Long monthlyBudgetAmount = monthlyBudget.getAmount();
        // 예정되어 있는 고정 지출 조회 (아직 발생하지 않은)
        long pendingExpenseAmount = Optional.ofNullable(scheduledTransactionService.getTotalPendingScheduledTransactionAmountByMonth(user, year, month)).orElse(0L);
        // 현재까지 발생한 지출 조회
        long totalExpenseAmount = Optional.ofNullable(expenseService.getTotalExpenseAmountForMonth(user, startOfMonth, endOfMonth)).orElse(0L);
        // 남은 예산 계산
        long remainingBudgetAmount = Math.max(monthlyBudgetAmount - pendingExpenseAmount - totalExpenseAmount, 0); // 음수 방지

        // 하루 예산 계산 (남은 일 수 기준)
        long remainingDays = today.until(endOfMonth).getDays() + 1; // 오늘 포함
        Long dailyBudgetAmount = remainingDays > 0 ? remainingBudgetAmount / remainingDays : 0;
        List<CategoryBudgetProgressUnitDto> categoryBudgets = categoryBudgetService.getCategoryBudgets(user, monthlyBudget, startOfMonth, endOfMonth);

        return BudgetProgressResponseDto.builder()
                .remainingBudgetAmount(remainingBudgetAmount)
                .dailyBudgetAmount(dailyBudgetAmount)
                .monthlyBudgetAmount(monthlyBudgetAmount)
                .pendingExpenseAmount(pendingExpenseAmount)
                .totalExpenseAmount(totalExpenseAmount)
                .categoryBudgets(categoryBudgets)
                .build();
    }

    public Object getBudgetPlan(@NotNull @Min(1900) int year, @NotNull @Min(1) @Max(12) int month) {
    }

    public Object modifyBudgetPlan(@Valid BudgetModifyRequestDto budgetModifyRequestDto) {
    }
}
