package tamtam.mooney.domain.budget.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.domain.budget.entity.MonthlyComment;

public interface MonthlyCommentRepository extends JpaRepository<MonthlyComment, Long> {
}
