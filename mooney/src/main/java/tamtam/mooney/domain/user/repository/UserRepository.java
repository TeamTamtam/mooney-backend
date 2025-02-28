package tamtam.mooney.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
