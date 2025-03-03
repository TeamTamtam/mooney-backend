package tamtam.mooney.domain.budget.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.budget.dto.CategoryBudgetPlanUnitDto;
import tamtam.mooney.domain.budget.dto.CategoryBudgetSimpleUnitDto;
import tamtam.mooney.domain.budget.dto.CategoryBudgetProgressUnitDto;
import tamtam.mooney.domain.budget.entity.CategoryBudget;
import tamtam.mooney.domain.budget.entity.MonthlyBudget;
import tamtam.mooney.domain.budget.repository.CategoryBudgetRepository;
import tamtam.mooney.domain.transaction.entity.ExpenseCategory;
import tamtam.mooney.domain.transaction.service.TransactionService;
import tamtam.mooney.domain.user.entity.User;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryBudgetService {
    private final CategoryBudgetRepository categoryBudgetRepository;
    private final TransactionService transactionService;

    public void saveCategoryBudgets(MonthlyBudget monthlyBudget, List<CategoryBudgetSimpleUnitDto> categoryBudgets) {
        Optional.ofNullable(categoryBudgets)
                .filter(list -> !list.isEmpty())
                .ifPresent(list -> {
                    // 요청된 카테고리 예산을 맵으로 변환
                    Map<ExpenseCategory, Long> categoryBudgetMap = list.stream()
                            .collect(Collectors.toMap(CategoryBudgetSimpleUnitDto::expenseCategory, CategoryBudgetSimpleUnitDto::amount));

                    // 모든 ExpenseCategory를 순회하며 예산 저장
                    List<CategoryBudget> categoryBudgetList = Arrays.stream(ExpenseCategory.values())
                            .map(ec -> CategoryBudget.builder()
                                    .monthlyBudget(monthlyBudget)
                                    .expenseCategory(ec)
                                    .amount(categoryBudgetMap.getOrDefault(ec, 0L))
                                    .build())
                            .collect(Collectors.toList());

                    categoryBudgetRepository.saveAll(categoryBudgetList);
                });
    }

    @Transactional(readOnly = true)
    public List<CategoryBudget> findByMonthlyBudget(MonthlyBudget monthlyBudget) {
        return categoryBudgetRepository.findByMonthlyBudget(monthlyBudget);
    }

    @Transactional(readOnly = true)
    public List<CategoryBudgetProgressUnitDto> getCategoryBudgetProgresses(User user, MonthlyBudget monthlyBudget, LocalDate startOfMonth, LocalDate endOfMonth) {
        // 요청된 월의 카테고리별 예산 조회
        List<CategoryBudget> budgets = findByMonthlyBudget(monthlyBudget);

        // 각 카테고리의 실제 지출 계산
        return budgets.stream()
                .map(cb -> {
                    Long spent = transactionService.getTotalExpenseForCategory(user, cb.getExpenseCategory(), startOfMonth, endOfMonth);
                    int spentPercentage = (int) ((spent * 100.0) / cb.getAmount());
                    long remaining = cb.getAmount() - spent;
                    remaining = Math.max(remaining, 0);

                    return new CategoryBudgetProgressUnitDto(
                            cb.getExpenseCategory(),
                            cb.getAmount(),
                            spent,
                            spentPercentage,
                            remaining
                    );
                }).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CategoryBudgetPlanUnitDto> getCategoryBudgetPlans(User user, MonthlyBudget monthlyBudget, LocalDate startOfMonth) {
        // 지난달의 시작일과 종료일 계산
        LocalDate lastMonthStart = startOfMonth.minusMonths(1);
        LocalDate lastMonthEnd = lastMonthStart.withDayOfMonth(lastMonthStart.lengthOfMonth());

        // 요청된 월의 카테고리별 예산 조회
        List<CategoryBudget> budgets = findByMonthlyBudget(monthlyBudget);

        // 각 카테고리의 지난달 지출과 이번달 예산을 조회
        return budgets.stream()
                .map(cb -> {
                    long lastMonthExpenseAmount = transactionService.getTotalExpenseForCategory(user, cb.getExpenseCategory(), lastMonthStart, lastMonthEnd);

                    return new CategoryBudgetPlanUnitDto(
                            cb.getCategoryBudgetId(),
                            cb.getExpenseCategory(),
                            lastMonthExpenseAmount,
                            cb.getAmount()
                    );
                }).collect(Collectors.toList());
    }
}
