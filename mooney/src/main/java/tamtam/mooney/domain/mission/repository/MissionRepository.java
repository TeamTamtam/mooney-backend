package tamtam.mooney.domain.mission.repository;

import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import reactor.core.publisher.Mono;
import tamtam.mooney.domain.mission.entity.Mission;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MissionRepository extends JpaRepository<Mission, Long> {
    Mono<Map<String, Float>> findVisitDataByCategory(Long userId, String category);

    Mono<Map<String, Float>> findSpendingDataByCategory(Long userId, String category);

    Float findMissionResultById(Long missionId);

    List<Mission> getMissionByCategoryBudget(Long categoryBudgetId);

    // 1️⃣ 사용자의 이번 주 미션 가져오기
    @Query("SELECT m FROM Mission m " +
            "WHERE m.categoryBudget.monthlyBudget.user.userId = :userId " +
            "AND :today BETWEEN m.startDate AND m.endDate")
    List<Mission> findWeeklyMissionsByUser(@Param("userId") Long userId, @Param("today") LocalDate today);

    // 2️⃣ 해당 미션에 맞는 사용자 ID 가져오기
    @Query("SELECT mb.user.userId FROM Mission m " +
            "JOIN m.categoryBudget cb " +
            "JOIN cb.monthlyBudget mb " +
            "WHERE m.missionId = :missionId")
    Optional<Long> findUserIdByMissionId(@Param("missionId") Long missionId);


}
