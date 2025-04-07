package tamtam.mooney.domain.mission.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.domain.user.repository.UserRepository;
import tamtam.mooney.domain.user.service.UserService;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Component
public class MissionScheduler {

    private final MissionService missionService;
    private final UserRepository userRepository;

    public MissionScheduler(MissionService missionService, UserRepository userRepository) {
        this.missionService = missionService;
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "00 50 23 * * 0") // 매주 일요일 23:50:00에 실행
    //@Scheduled(cron = "10 * * * * *")
    public void scheduleWeeklyMissionGeneration() {
        LocalDate startDate = getNextMonday();
        System.out.println("미션 자동 생성 시작: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        List<User> users = userRepository.findAll(); // 🔥 모든 사용자 조회

        for (User user : users) {
            try {
                List<String> missionTitles = missionService.generateWeeklyMissions(user, startDate); // 🔁 사용자마다 미션 생성
                // 생성된 미션 확인 로그
                System.out.println("생성된 미션 목록: " + missionTitles);
            } catch (Exception e) {
                log.warn("미션 생성 실패 - userId: {}, error: {}", user.getUserId(), e.getMessage());
            }
        }
    }

    // ✅ 수동 실행을 위한 메서드 추가
    public List<String>  runSchedulerManually(User user, LocalDate startDate) {
        System.out.println("⚡ 수동 실행: 미션 자동 생성 시작 (startDate: " + startDate + ")");
        return missionService.generateWeeklyMissions(user, startDate);
    }

    // ✅ 다음 주 월요일을 구하는 메서드
    private LocalDate getNextMonday() {
        LocalDate today = LocalDate.now();
        return today.with(DayOfWeek.MONDAY).plusWeeks(1); // 다음 주 월요일
    }
}

