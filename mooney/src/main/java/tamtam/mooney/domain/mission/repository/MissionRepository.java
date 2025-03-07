package tamtam.mooney.domain.mission.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import reactor.core.publisher.Mono;
import tamtam.mooney.domain.mission.entity.Mission;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface MissionRepository extends JpaRepository<Mission, Long> {
    Mono<Map<String, Float>> findVisitDataByCategory(Long userId, String category);

    Mono<Map<String, Float>> findSpendingDataByCategory(Long userId, String category);

    Float findMissionResultById(Long missionId);

    List<Mission> getMissionByCategoryBudget(Long categoryBudgetId);

    //그 userId의 today가 startDate ~ 7 내에 있게
    List<Mission> findByUserIdAndStartDateLessThanEqualAndEndDateGreaterThanEqual(Long userId, LocalDate today, LocalDate today2);


}
