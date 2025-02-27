package tamtam.mooney.domain.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.domain.transaction.entity.Expense;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
}
