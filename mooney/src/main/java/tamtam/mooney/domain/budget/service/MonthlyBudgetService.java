package tamtam.mooney.domain.budget.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.budget.dto.CategoryBudgetProgressUnitDto;
import tamtam.mooney.domain.budget.dto.FirstBudgetRequestDto;
import tamtam.mooney.domain.budget.dto.MonthlyBudgetProgressResponseDto;
import tamtam.mooney.domain.budget.entity.CategoryBudget;
import tamtam.mooney.domain.budget.entity.MonthlyBudget;
import tamtam.mooney.domain.budget.repository.MonthlyBudgetRepository;
import tamtam.mooney.domain.transaction.service.ExpenseService;
import tamtam.mooney.domain.transaction.service.RecurringTransactionService;
import tamtam.mooney.domain.transaction.service.ScheduledTransactionService;
import tamtam.mooney.domain.transaction.service.TransactionService;
import tamtam.mooney.domain.user.dto.UserHomeWeeklyBudgetDto;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.domain.user.service.UserService;
import tamtam.mooney.global.exception.CustomException;
import tamtam.mooney.global.exception.ErrorCode;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MonthlyBudgetService {
    private final MonthlyBudgetRepository monthlyBudgetRepository;
    private final UserService userService;
    private final RecurringTransactionService recurringTransactionService;
    private final CategoryBudgetService categoryBudgetService;
    private final ExpenseService expenseService;
    private final TransactionService transactionService;
    private final ScheduledTransactionService scheduledTransactionService;


    public void saveFirstBudget(FirstBudgetRequestDto requestDto) {
        User user = userService.getCurrentUser();

        // 월 예산 저장
        MonthlyBudget monthlyBudget = MonthlyBudget.builder()
                .user(user)
                .monthDate(requestDto.monthDate())
                .amount(requestDto.monthlyBudgetAmount())
                .build();
        monthlyBudgetRepository.save(monthlyBudget);

        // 고정 수입, 지출, 저축 저장
        recurringTransactionService.saveRecurringTransactions(user, requestDto.fixedIncome(), "INCOME");
        recurringTransactionService.saveRecurringTransactions(user, requestDto.fixedExpense(), "EXPENSE");
        recurringTransactionService.saveRecurringTransactions(user, requestDto.fixedSavings(), "SAVINGS");

        // 카테고리별 예산 저장
        categoryBudgetService.saveCategoryBudgets(monthlyBudget, requestDto.categoryBudgets());
    }

    @Transactional(readOnly = true)
    public MonthlyBudget getMonthlyBudget(User user, LocalDate monthDate) {
        return monthlyBudgetRepository.findByUserAndMonthDate(user, monthDate)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public MonthlyBudgetProgressResponseDto getMonthlyBudgetProgress(int year, int month, LocalDate today) {
        User user = userService.getCurrentUser();
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        // 월 예산 총액
        MonthlyBudget monthlyBudget = getMonthlyBudget(user, startOfMonth);
        Long totalBudget = monthlyBudget.getAmount();

        // 예정되어 있는 고정 지출 조회
        Long recurringExpense = scheduledTransactionService.getTotalPendingScheduledTransactionAmountByMonth(user, year, month);

        // 현재까지 발생한 지출 조회
        Long totalExpense = expenseService.getTotalExpenseAmountForMonth(user, startOfMonth, endOfMonth);

        // 남은 예산 계산
        long remainingBudget = totalBudget - recurringExpense - totalExpense;
        remainingBudget = Math.max(remainingBudget, 0); // 음수 방지

        // 하루 예산 계산 (남은 일 수 기준)
        long remainingDays = today.until(endOfMonth).getDays() + 1; // 오늘 포함
        Long dailyBudget = remainingDays > 0 ? remainingBudget / remainingDays : 0;
        List<CategoryBudgetProgressUnitDto> categoryBudgets = getCategoryBudgets(user, monthlyBudget, startOfMonth, endOfMonth);

        return MonthlyBudgetProgressResponseDto.builder()
                .remainingBudget(remainingBudget)
                .dailyBudget(dailyBudget)
                .monthlyBudget(totalBudget)
                .recurringExpense(recurringExpense)
                .totalExpense(totalExpense)
                .categoryBudgets(categoryBudgets)
                .build();
    }

    private List<CategoryBudgetProgressUnitDto> getCategoryBudgets(User user, MonthlyBudget monthlyBudget, LocalDate startOfMonth, LocalDate endOfMonth) {
        // 요청된 월의 카테고리별 예산 조회
        List<CategoryBudget> budgets = categoryBudgetService.findByMonthlyBudget(monthlyBudget);

        // 각 카테고리의 실제 지출 계산
        return budgets.stream()
                .map(budget -> {
                    Long spent = transactionService.getTotalExpenseForCategory(user, budget.getExpenseCategory(), startOfMonth, endOfMonth);
                    int spentPercentage = (int) ((spent * 100.0) / budget.getAmount());
                    long remaining = budget.getAmount() - spent;
                    remaining = Math.max(remaining, 0);

                    return new CategoryBudgetProgressUnitDto(
                            budget.getExpenseCategory().getIcon(),
                            budget.getExpenseCategory().getCategoryName(),
                            budget.getAmount(),
                            spent,
                            spentPercentage,
                            remaining
                    );
                }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserHomeWeeklyBudgetDto getWeeklyBudgetInfo(User user, LocalDate today) {
        return null;
    }
}
