package tamtam.mooney.domain.mission.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tamtam.mooney.domain.mission.dto.MissionDto;
import tamtam.mooney.domain.mission.dto.MissionTabDto;
import tamtam.mooney.domain.mission.service.MissionScheduler;
import tamtam.mooney.domain.mission.service.MissionService;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.domain.user.service.UserService;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Tag(name = "Mission")
@RestController
@RequestMapping("/missions")
@RequiredArgsConstructor
public class MissionController {

    private final MissionService missionService;
    private final MissionScheduler missionScheduler;
    private final UserService userService;

    @Operation(summary = "미션창 조회(배경, 미션 현황 업데이트)")
    @GetMapping("")
    public ResponseEntity<MissionTabDto> getMissionResultByDate() {
        LocalDate today = LocalDate.now();
        List<MissionDto> weeklyMissions =  missionService.getWeeklyMissionsDetail(today);
        Float mooneyStatus = missionService.updateMissionResult(today);

        MissionTabDto missionTabDto = new MissionTabDto(weeklyMissions, mooneyStatus);
        return ResponseEntity.ok(missionTabDto);
    }

    @Operation(summary = "해당 사용자에 대해서만 미션 스케줄링 강제로 수행하기")
    @PostMapping("/run")
    public ResponseEntity<List<String>> getNewMission(@RequestParam LocalDate startDate) {
        long startTime = System.currentTimeMillis(); // 시작 시간 기록
        User user = userService.getCurrentUser();
        List<String> newMissions = missionScheduler.runSchedulerManually(user, startDate);
        long endTime = System.currentTimeMillis();   // 끝 시간 기록
        long duration = endTime - startTime;

        log.info("미션 스케줄러 수동 실행 시간: {}ms", duration);
        return ResponseEntity.ok(newMissions);
    }




}
