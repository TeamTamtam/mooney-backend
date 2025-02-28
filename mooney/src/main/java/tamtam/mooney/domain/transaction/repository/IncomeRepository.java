package tamtam.mooney.domain.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.domain.transaction.entity.Income;

public interface IncomeRepository extends JpaRepository<Income, Long> {
}
