package tamtam.mooney.domain.budget.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.domain.budget.entity.MonthlyBudget;
import tamtam.mooney.domain.user.entity.User;

import java.time.LocalDate;
import java.util.Optional;

public interface MonthlyBudgetRepository extends JpaRepository<MonthlyBudget, Long> {
    Optional<MonthlyBudget> findByUserAndMonthDate(User user, LocalDate startOfMonth);
}
