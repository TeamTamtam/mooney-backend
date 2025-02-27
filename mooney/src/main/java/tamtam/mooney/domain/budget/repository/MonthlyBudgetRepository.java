package tamtam.mooney.domain.budget.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.domain.budget.entity.MonthlyBudget;

public interface MonthlyBudgetRepository extends JpaRepository<MonthlyBudget, Long> {
}
