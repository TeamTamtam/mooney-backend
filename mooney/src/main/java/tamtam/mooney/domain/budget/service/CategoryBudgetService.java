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
import tamtam.mooney.domain.transaction.service.TransactionService;
import tamtam.mooney.domain.user.entity.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryBudgetService {
    private final CategoryBudgetRepository categoryBudgetRepository;
    private final TransactionService transactionService;

    public void saveCategoryBudgets(MonthlyBudget monthlyBudget, List<CategoryBudgetSimpleUnitDto> categoryBudgets) {
        List<CategoryBudget> budgetList = categoryBudgets.stream()
                .map(dto -> new CategoryBudget(monthlyBudget, dto.expenseCategory(), dto.amount()))
                .toList();
        categoryBudgetRepository.saveAll(budgetList); // Batch insert 사용
    }

    @Transactional(readOnly = true)
    public List<CategoryBudget> findByMonthlyBudget(MonthlyBudget monthlyBudget) {
        return categoryBudgetRepository.findByMonthlyBudget(monthlyBudget);
    }

    @Transactional(readOnly = true)
    public List<CategoryBudgetProgressUnitDto> getCategoryBudgetProgresses(User user, MonthlyBudget monthlyBudget, LocalDate startOfMonth, LocalDate endOfMonth) {
        // 요청된 월의 카테고리별 예산 조회
        List<CategoryBudget> budgets = findByMonthlyBudget(monthlyBudget);

        // 특정 기간 동안의 모든 카테고리별 총 지출
        Map<String, Long> totalExpensesByCategory = transactionService.mapTotalExpenseForAllCategories(user, startOfMonth, endOfMonth);

        // 각 카테고리의 실제 지출 계산
        return budgets.stream()
                .map(cb -> {
                    Long spent = totalExpensesByCategory.getOrDefault(cb.getExpenseCategory().name(), 0L);
                    int spentPercentage = cb.getAmount() > 0 ? (int) ((spent * 100.0) / cb.getAmount()) : 0;
                    long remaining = Math.max(cb.getAmount() - spent, 0);

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

        // 지난달 모든 카테고리의 총 지출
        Map<String, Long> lastMonthExpensesByCategory = transactionService.mapTotalExpenseForAllCategories(user, lastMonthStart, lastMonthEnd);

        // 각 카테고리별 예산 계획 생성
        return budgets.stream()
                .map(cb -> new CategoryBudgetPlanUnitDto(
                        cb.getCategoryBudgetId(),
                        cb.getExpenseCategory(),
                        lastMonthExpensesByCategory.getOrDefault(cb.getExpenseCategory().name(), 0L),
                        cb.getAmount()
                )).collect(Collectors.toList());
    }
}
