package tamtam.mooney.domain.budget.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.domain.budget.entity.CategoryBudget;
import tamtam.mooney.domain.budget.entity.MonthlyBudget;

import java.util.List;

public interface CategoryBudgetRepository extends JpaRepository<CategoryBudget, Long> {
    // 특정 월 예산에 속한 카테고리 예산 조회
    List<CategoryBudget> findByMonthlyBudget(MonthlyBudget monthlyBudget);
}
