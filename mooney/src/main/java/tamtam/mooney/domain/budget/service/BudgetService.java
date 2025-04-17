package tamtam.mooney.domain.budget.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.budget.dto.*;
import tamtam.mooney.domain.budget.entity.CategoryBudget;
import tamtam.mooney.domain.budget.entity.MonthlyBudget;
import tamtam.mooney.domain.budget.repository.MonthlyBudgetRepository;
import tamtam.mooney.domain.enums.ExpenseCategory;
import tamtam.mooney.domain.transaction.entity.ScheduledTransaction;
import tamtam.mooney.domain.transaction.service.ExpenseService;
import tamtam.mooney.domain.transaction.service.RecurringTransactionService;
import tamtam.mooney.domain.transaction.service.ScheduledTransactionService;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.domain.user.service.UserService;
import tamtam.mooney.global.exception.CustomException;
import tamtam.mooney.global.exception.ErrorCode;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BudgetService {
    private final MonthlyBudgetRepository monthlyBudgetRepository;
    private final MonthlyBudgetService monthlyBudgetService;
    private final CategoryBudgetService categoryBudgetService;
    private final UserService userService;
    private final RecurringTransactionService recurringTransactionService;
    private final ScheduledTransactionService scheduledTransactionService;
    private final ExpenseService expenseService;

    // 첫 예산 수립
    public void saveFirstBudget(FirstBudgetRequestDto requestDto) {
        User user = userService.getCurrentUser();
        MonthlyBudget monthlyBudget = saveOrUpdateBudget(user, requestDto.year(), requestDto.month(), requestDto.monthlyBudgetAmount());

        // 고정 수입, 지출, 저축 저장
        recurringTransactionService.saveRecurringTransactions(user, requestDto.fixedIncome(), "INCOME");
        recurringTransactionService.saveRecurringTransactions(user, requestDto.fixedExpense(), "EXPENSE");
        recurringTransactionService.saveRecurringTransactions(user, requestDto.fixedSavings(), "SAVINGS");

        // 카테고리별 예산 저장
        categoryBudgetService.saveCategoryBudgets(monthlyBudget, requestDto.categoryBudgets());
    }

    // 다음달 예산 수립
//    public void saveNextMonthBudget(NextMonthBudgetRequestDto requestDto) {
//        User user = userService.getCurrentUser();
//        MonthlyBudget monthlyBudget = saveOrUpdateBudget(user, requestDto.year(), requestDto.month(), requestDto.monthlyBudgetAmount());
//
//        // 기존 고정 수입/지출/저축을 유지하면서 업데이트
//        recurringTransactionService.updateRecurringTransactions(user, requestDto.fixedIncome(), "INCOME");
//        recurringTransactionService.updateRecurringTransactions(user, requestDto.fixedExpense(), "EXPENSE");
//        recurringTransactionService.updateRecurringTransactions(user, requestDto.fixedSavings(), "SAVINGS");
//
//        // 카테고리별 예산 수정
//        categoryBudgetService.updateCategoryBudgets(monthlyBudget, requestDto.categoryBudgets());
//    }

    // 예산 수립
    public MonthlyBudget saveOrUpdateBudget(User user, int year, int month, long monthlyBudgetAmount) {
        LocalDate startOfMonth = LocalDate.of(year, month, 1);

        // 기존 예산 존재 여부 확인 후 업데이트 또는 새로 저장
        return monthlyBudgetRepository.findByUserAndMonthDate(user, startOfMonth)
                .map(existingBudget -> {
                    existingBudget.updateAmount(monthlyBudgetAmount);
                    return existingBudget;
                })
                .orElseGet(() -> {
                    MonthlyBudget newBudget = MonthlyBudget.builder()
                            .user(user)
                            .monthDate(startOfMonth)
                            .amount(monthlyBudgetAmount)
                            .build();
                    return monthlyBudgetRepository.save(newBudget);
                });
    }

    @Transactional(readOnly = true)
    public BudgetProgressResponseDto getBudgetProgress(int year, int month, LocalDate today) {
        User user = userService.getCurrentUser();
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        // 월 예산 총액
        MonthlyBudget monthlyBudget = monthlyBudgetService.getMonthlyBudget(user, startOfMonth);
        long monthlyBudgetAmount = monthlyBudget.getAmount();
        // 예정되어 있는 고정 지출 조회 (아직 발생하지 않은)
        long pendingExpenseAmount = Optional.ofNullable(scheduledTransactionService.getTotalPendingScheduledTransactionAmountByMonth(user, startOfMonth, endOfMonth)).orElse(0L);
        // 현재까지 발생한 지출 조회
        long totalExpenseAmount = Optional.ofNullable(expenseService.getTotalExpenseAmountForPeriod(user, startOfMonth, endOfMonth)).orElse(0L);
        // 남은 예산 계산
        long remainingBudgetAmount = Math.max(monthlyBudgetAmount - pendingExpenseAmount - totalExpenseAmount, 0); // 음수 방지

        // 하루 예산 계산 (남은 일 수 기준)
        long remainingDays = today.until(endOfMonth).getDays() + 1; // 오늘 포함
        Long dailyBudgetAmount = remainingDays > 0 ? remainingBudgetAmount / remainingDays : 0;
        List<CategoryBudgetProgressUnitDto> categoryBudgets = categoryBudgetService.getCategoryBudgetProgresses(user, monthlyBudget, startOfMonth);

        // 사용한 예산 비율 계산
        int budgetUsagePercentage = monthlyBudgetAmount > 0 ? (int) ((totalExpenseAmount + pendingExpenseAmount) * 100 / monthlyBudgetAmount) : 0;

        return BudgetProgressResponseDto.builder()
                .remainingBudgetAmount(remainingBudgetAmount)
                .dailyBudgetAmount(dailyBudgetAmount)
                .budgetUsagePercentage(budgetUsagePercentage)
                .monthlyBudgetAmount(monthlyBudgetAmount)
                .pendingExpenseAmount(pendingExpenseAmount)
                .totalExpenseAmount(totalExpenseAmount)
                .categoryBudgets(categoryBudgets)
                .build();
    }

    // 예산 계획 조회
    @Transactional(readOnly = true)
    public BudgetPlanResponseDto getBudgetPlan(int year, int month) {
        User user = userService.getCurrentUser();
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        // 월 예산 총액
        MonthlyBudget monthlyBudget = monthlyBudgetService.getMonthlyBudget(user, startOfMonth);
        long monthlyBudgetAmount = monthlyBudget.getAmount();
        // 카테고리별 예산
        List<CategoryBudgetPlanUnitDto> categoryBudgets = categoryBudgetService.getCategoryBudgetPlans(user, monthlyBudget, startOfMonth);
        // 이번 달 고정비 가져오기
        List<ScheduledTransaction> scheduledTransactions = scheduledTransactionService.getScheduledExpensesByMonth(user, startOfMonth, endOfMonth);
        // 고정 지출(EXPENSE) 및 저축(SAVINGS) 변환
        List<BudgetPlanRecurringTransactionUnitDto> fixedExpenses = scheduledTransactions.stream()
                .filter(st -> "EXPENSE".equals(st.getRecurringTransaction().getRecurringType()))
                .map(st -> new BudgetPlanRecurringTransactionUnitDto(
                        st.getRecurringTransaction().getTitle(),
                        st.getRecurringTransaction().getAmount()
                ))
                .toList();
        List<BudgetPlanRecurringTransactionUnitDto> fixedSavings = scheduledTransactions.stream()
                .filter(st -> "SAVINGS".equals(st.getRecurringTransaction().getRecurringType()))
                .map(st -> new BudgetPlanRecurringTransactionUnitDto(
                        st.getRecurringTransaction().getTitle(),
                        st.getRecurringTransaction().getAmount()
                ))
                .toList();

        return BudgetPlanResponseDto.builder()
                .monthlyBudgetAmount(monthlyBudgetAmount)
                .fixedExpense(fixedExpenses)
                .fixedSavings(fixedSavings)
                .categoryBudgets(categoryBudgets)
                .build();
    }

    // 예산 계획 수정
    public void modifyBudgetPlan(BudgetModifyRequestDto requestDto) {
        User user = userService.getCurrentUser();
        LocalDate startOfMonth = LocalDate.of(requestDto.year(), requestDto.month(), 1);

        // 1. 월 예산 수정
        MonthlyBudget monthlyBudget = monthlyBudgetService.getMonthlyBudget(user, startOfMonth);
        monthlyBudget.updateAmount(requestDto.monthlyBudgetAmount());

        // 2. 기존 카테고리별 예산 가져오기
        List<CategoryBudget> existingCategoryBudgets = categoryBudgetService.findByMonthlyBudget(monthlyBudget);
        Map<ExpenseCategory, CategoryBudget> categoryBudgetMap = existingCategoryBudgets.stream()
                .collect(Collectors.toMap(CategoryBudget::getExpenseCategory, budget -> budget));

        // 3. 새로운 카테고리 예산 요청 처리
        for (CategoryBudgetSimpleUnitDto newBudgetDto : requestDto.categoryBudgets()) {
            CategoryBudget existingBudget = categoryBudgetMap.get(newBudgetDto.expenseCategory());
            if (existingBudget != null) {
                // 기존 카테고리 예산이 존재하면 업데이트
                existingBudget.updateAmount(newBudgetDto.amount());
            } else {
                throw new CustomException(ErrorCode.RESOURCE_NOT_FOUND);
            }
        }
    }

}
