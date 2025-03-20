package tamtam.mooney.domain.mission.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
public class MissionScheduler {

    private final MissionService missionService;

    public MissionScheduler(MissionService missionService) {
        this.missionService = missionService;
    }

    @Scheduled(cron = "00 50 23 * * SUN") // 매주 일요일 23:50:00에 실행
    public List<String> scheduleWeeklyMissionGeneration() {
        LocalDate startDate = getNextMonday();
        System.out.println("미션 자동 생성 시작: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        // 미션 생성
        List<String> missionTitles = missionService.generateWeeklyMissions(startDate);

        // 생성된 미션 확인 로그
        System.out.println("생성된 미션 목록: " + missionTitles);

        return missionTitles;
    }

    // ✅ 수동 실행을 위한 메서드 추가
    public List<String>  runSchedulerManually(LocalDate startDate) {
        System.out.println("⚡ 수동 실행: 미션 자동 생성 시작 (startDate: " + startDate + ")");
        return missionService.generateWeeklyMissions(startDate);
    }

    // ✅ 다음 주 월요일을 구하는 메서드
    private LocalDate getNextMonday() {
        LocalDate today = LocalDate.now();
        return today.with(DayOfWeek.MONDAY).plusWeeks(1); // 다음 주 월요일
    }
}

