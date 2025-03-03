package tamtam.mooney.domain.budget.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.budget.dto.CategoryBudgetDto;
import tamtam.mooney.domain.budget.entity.CategoryBudget;
import tamtam.mooney.domain.budget.entity.MonthlyBudget;
import tamtam.mooney.domain.budget.repository.CategoryBudgetRepository;
import tamtam.mooney.domain.transaction.entity.ExpenseCategory;

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

    public void saveCategoryBudgets(MonthlyBudget monthlyBudget, List<CategoryBudgetDto> categoryBudgets) {
        Optional.ofNullable(categoryBudgets)
                .filter(list -> !list.isEmpty())
                .ifPresent(list -> {
                    // 요청된 카테고리 예산을 맵으로 변환
                    Map<ExpenseCategory, Long> categoryBudgetMap = list.stream()
                            .collect(Collectors.toMap(CategoryBudgetDto::expenseCategory, CategoryBudgetDto::amount));

                    // 모든 ExpenseCategory를 순회하며 예산 저장
                    List<CategoryBudget> categoryBudgetList = Arrays.stream(ExpenseCategory.values())
                            .map(category -> CategoryBudget.builder()
                                    .monthlyBudget(monthlyBudget)
                                    .expenseCategory(category)
                                    .amount(categoryBudgetMap.getOrDefault(category, 0L))
                                    .build())
                            .collect(Collectors.toList());

                    categoryBudgetRepository.saveAll(categoryBudgetList);
                });
    }

    @Transactional(readOnly = true)
    public List<CategoryBudget> findByMonthlyBudget(MonthlyBudget monthlyBudget) {
        return categoryBudgetRepository.findByMonthlyBudget(monthlyBudget);
    }
}
