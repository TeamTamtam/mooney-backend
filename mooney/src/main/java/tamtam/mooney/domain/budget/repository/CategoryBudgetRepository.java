package tamtam.mooney.domain.budget.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.domain.budget.entity.CategoryBudget;

public interface CategoryBudgetRepository extends JpaRepository<CategoryBudget, Long> {
}
