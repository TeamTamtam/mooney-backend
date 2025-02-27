package tamtam.mooney.domain.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.domain.transaction.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
}
