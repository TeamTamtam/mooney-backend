package tamtam.mooney.domain.budget.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tamtam.mooney.domain.budget.entity.CategoryBudget;
import tamtam.mooney.domain.budget.entity.MonthlyBudget;
import tamtam.mooney.domain.enums.ExpenseCategory;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CategoryBudgetRepository extends JpaRepository<CategoryBudget, Long> {
    // 특정 월 예산에 속한 카테고리 예산 조회
    List<CategoryBudget> findCategoryBudgetByMonthlyBudget(MonthlyBudget monthlyBudget);


    // 특정 사용자의 특정 월(monthDate)에 해당하는 특정 카테고리 예산 조회
    @Query("SELECT cb FROM CategoryBudget cb WHERE cb.monthlyBudget IN " +
            "(SELECT mb FROM MonthlyBudget mb WHERE mb.user.userId = :userId AND mb.monthDate = :monthDate) " +
            "AND cb.expenseCategory = :category")
    CategoryBudget findCategoryBudgetByUserIdAndExpenseCategoryAndMonth(@Param("userId") Long userId,
                                                                 @Param("category") ExpenseCategory category,
                                                                 @Param("monthDate") LocalDate monthDate);
    CategoryBudget findCategoryBudgetByMonthlyBudgetAndExpenseCategory(MonthlyBudget monthlyBudget, ExpenseCategory category);
}
