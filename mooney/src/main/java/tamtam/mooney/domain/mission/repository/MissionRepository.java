package tamtam.mooney.domain.mission.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tamtam.mooney.domain.mission.entity.Mission;

public interface MissionRepository extends JpaRepository<Mission, Long> {
}
