package tamtam.mooney.domain.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.domain.transaction.entity.RecurringTransaction;

public interface RecurringTransactionRepository extends JpaRepository<RecurringTransaction, Long> {
}
