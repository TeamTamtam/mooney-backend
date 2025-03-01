package tamtam.mooney.domain.budget.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.budget.dto.FirstBudgetRequestDto;
import tamtam.mooney.domain.budget.entity.MonthlyBudget;
import tamtam.mooney.domain.budget.repository.MonthlyBudgetRepository;
import tamtam.mooney.domain.transaction.service.RecurringTransactionService;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.domain.user.service.UserService;

@Service
@Transactional
@RequiredArgsConstructor
public class MonthlyBudgetService {
    private final MonthlyBudgetRepository monthlyBudgetRepository;
    private final UserService userService;
    private final RecurringTransactionService recurringTransactionService;
    private final CategoryBudgetService categoryBudgetService;

    public void saveFirstBudget(FirstBudgetRequestDto requestDto) {
        User user = userService.getCurrentUser();

        // 1. 월 예산 저장
        MonthlyBudget monthlyBudget = MonthlyBudget.builder()
                .user(user)
                .monthDate(requestDto.monthDate())
                .amount(requestDto.monthlyBudgetAmount())
                .build();
        monthlyBudgetRepository.save(monthlyBudget);

        // 2. 고정 수입, 지출, 저축 저장
        recurringTransactionService.saveRecurringTransactions(user, requestDto.fixedIncome(), "INCOME");
        recurringTransactionService.saveRecurringTransactions(user, requestDto.fixedExpense(), "EXPENSE");
        recurringTransactionService.saveRecurringTransactions(user, requestDto.fixedSavings(), "SAVINGS");

        // 3. 카테고리별 예산 저장
        categoryBudgetService.saveCategoryBudgets(monthlyBudget, requestDto.categoryBudgets());
    }
}
