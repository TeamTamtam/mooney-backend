package tamtam.mooney.domain.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.domain.transaction.entity.Income;
import tamtam.mooney.domain.user.entity.User;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface IncomeRepository extends JpaRepository<Income, Long> {
    List<Income> findByUserAndTransactionTimeBetween(User user, LocalDateTime localDateTime, LocalDateTime localDateTime1);
}
