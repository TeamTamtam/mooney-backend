package tamtam.mooney.domain.mission.repository;

import io.lettuce.core.dynamic.annotation.Param;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tamtam.mooney.domain.mission.entity.Mission;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface MissionRepository extends JpaRepository<Mission, Long> {
    Float findMissionResultByMissionId(Long missionId);

//    List<Mission> getMissionByCategoryBudget_Id(Long categoryBudgetId);

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

    //현재 진행중인 미션의 place를 가져오기
    @Query("SELECT m.place FROM Mission m " +
            "WHERE m.categoryBudget.monthlyBudget.user.userId = :userId " +
            "AND :today BETWEEN m.startDate AND m.endDate")
    List<String> findWeeklyMissionPlacesByUser(@Param("userId") Long userId, @Param("today") LocalDate today);

    Mission findMissionByPlace(@NotNull String place);




}
