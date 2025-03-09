package tamtam.mooney.domain.mission.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tamtam.mooney.domain.mission.dto.MissionDto;
import tamtam.mooney.domain.mission.dto.MissionTabDto;
import tamtam.mooney.domain.mission.service.MissionService;
import tamtam.mooney.domain.transaction.dto.MonthlyTransactionDayUnitDto;
import tamtam.mooney.domain.user.dto.UserHomeWeeklyMissionDto;
import tamtam.mooney.domain.user.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Mission")
@RestController
@RequestMapping("/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;

    @Operation(summary = "미션창 조회(배경, 미션 현황 업데이트)")
    @GetMapping("/")
    public ResponseEntity<MissionTabDto> getMissionResultByDate(@RequestParam User user, @RequestParam @NotNull LocalDate date) {
        LocalDate today = LocalDate.now();
        List<MissionDto> weeklyMissions =  missionService.getWeeklyMissionsDetail(user, today);
        Float sum = 0.0F;
        for(MissionDto weeklyMission : weeklyMissions) {
            sum += weeklyMission.result();
        }
        Float mooneyStatus = sum / weeklyMissions.size();

        MissionTabDto missionTabDto = new MissionTabDto(weeklyMissions, mooneyStatus);
        return ResponseEntity.ok(missionTabDto);
    }

    @Operation(summary = "이번주 미션 받기")
    @GetMapping("/new")
    public ResponseEntity<List<UserHomeWeeklyMissionDto>> getNewMission(@RequestParam User user) {
        List<UserHomeWeeklyMissionDto> weeklyMissionDto = missionService.generateWeeklyMissions(user);
        return ResponseEntity.ok(weeklyMissionDto);
    }

    @Operation(summary = "홈 화면에서 미션 결과 조회")
    @GetMapping("/home")
    public ResponseEntity<List<UserHomeWeeklyMissionDto>> getMissionStatusHome(@RequestParam User user)
    {
        LocalDate today = LocalDate.now();
        List<UserHomeWeeklyMissionDto> weeklyMissions =  missionService.getWeeklyMissions(user, today);
        return ResponseEntity.ok(weeklyMissions);
    }






}
