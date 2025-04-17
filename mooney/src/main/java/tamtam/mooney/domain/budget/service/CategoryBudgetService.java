package tamtam.mooney.domain.budget.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.budget.dto.CategoryBudgetPlanUnitDto;
import tamtam.mooney.domain.budget.dto.CategoryBudgetSimpleUnitDto;
import tamtam.mooney.domain.budget.dto.CategoryBudgetProgressUnitDto;
import tamtam.mooney.domain.budget.entity.CategoryBudget;
import tamtam.mooney.domain.budget.entity.MonthlyBudget;
import tamtam.mooney.domain.budget.repository.CategoryBudgetRepository;
import tamtam.mooney.domain.enums.ExpenseCategory;
import tamtam.mooney.domain.transaction.service.ExpenseService;
import tamtam.mooney.domain.transaction.service.TransactionService;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.domain.user.service.UserService;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CategoryBudgetService {
    private final CategoryBudgetRepository categoryBudgetRepository;
    private final TransactionService transactionService;
    private final ExpenseService expenseService;

    public void saveCategoryBudgets(MonthlyBudget monthlyBudget, List<CategoryBudgetSimpleUnitDto> categoryBudgets) {
        Set<ExpenseCategory> existingCategories = categoryBudgets.stream()
                .map(CategoryBudgetSimpleUnitDto::expenseCategory)
                .collect(Collectors.toSet());

        List<CategoryBudget> budgetList = Stream.concat(
                categoryBudgets.stream().map(dto -> new CategoryBudget(monthlyBudget, dto.expenseCategory(), dto.amount())),
                Arrays.stream(ExpenseCategory.values())
                        .filter(category -> !existingCategories.contains(category))
                        .map(category -> new CategoryBudget(monthlyBudget, category, 0))
        ).toList();

        categoryBudgetRepository.saveAll(budgetList);
    }


    @Transactional(readOnly = true)
    public List<CategoryBudget> findByMonthlyBudget(MonthlyBudget monthlyBudget) {
        return categoryBudgetRepository.findCategoryBudgetByMonthlyBudget(monthlyBudget);
    }

    @Transactional(readOnly = true)
    public List<CategoryBudgetProgressUnitDto> getCategoryBudgetProgresses(User user, MonthlyBudget monthlyBudget, LocalDate startOfMonth) {
        // 요청된 월의 카테고리별 예산 조회
        List<CategoryBudget> budgets = findByMonthlyBudget(monthlyBudget);

        // 특정 기간 동안의 모든 카테고리별 총 지출
        Map<ExpenseCategory, Long> totalExpensesByCategory = expenseService.mapTotalExpenseForAllCategories(user, startOfMonth);

        // 각 카테고리의 실제 지출 계산
        return budgets.stream()
                .map(cb -> {
                    Long spent = totalExpensesByCategory.getOrDefault(cb.getExpenseCategory(), 0L);
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

        // 요청된 월의 카테고리별 예산 조회
        List<CategoryBudget> budgets = findByMonthlyBudget(monthlyBudget);

        // 지난달 모든 카테고리의 총 지출
        Map<ExpenseCategory, Long> lastMonthExpensesByCategory = expenseService.mapTotalExpenseForAllCategories(user, lastMonthStart);

        // 각 카테고리별 예산 계획 생성
        return budgets.stream()
                .map(cb -> new CategoryBudgetPlanUnitDto(
                        cb.getCategoryBudgetId(),
                        cb.getExpenseCategory(),
                        lastMonthExpensesByCategory.getOrDefault(cb.getExpenseCategory(), 0L),
                        cb.getAmount()
                )).collect(Collectors.toList());
    }
}
