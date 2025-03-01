package tamtam.mooney.domain.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.domain.transaction.entity.Expense;
import tamtam.mooney.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.List;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByUserAndTransactionTimeBetween(User user, LocalDateTime localDateTime, LocalDateTime localDateTime1);
}
