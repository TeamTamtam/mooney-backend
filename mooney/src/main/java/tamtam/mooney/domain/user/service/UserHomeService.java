package tamtam.mooney.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.agent.entity.Agent;
import tamtam.mooney.domain.agent.entity.UserAgent;
import tamtam.mooney.domain.budget.service.MonthlyBudgetService;
import tamtam.mooney.domain.transaction.dto.ExpenseUnitResponseDto;
import tamtam.mooney.domain.transaction.service.TransactionService;
import tamtam.mooney.domain.mission.service.MissionService;
import tamtam.mooney.domain.user.dto.UserHomeResponseDto;
import tamtam.mooney.domain.user.dto.UserHomeWeeklyBudgetDto;
import tamtam.mooney.domain.user.dto.UserHomeWeeklyMissionDto;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.domain.agent.service.UserAgentService;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserHomeService {

    private final UserService userService;
    private final TransactionService transactionService;
    private final MonthlyBudgetService monthlyBudgetService;
    private final MissionService missionService;
    private final UserAgentService userAgentService;

    @Transactional(readOnly = true)
    public UserHomeResponseDto getHomeInfo() {
        User user = userService.getCurrentUser();
        LocalDate today = LocalDate.now();

        // 인사말 생성
        UserAgent userAgent = userAgentService.getActiveUserAgent(user);
        String welcomeMessage = generateWelcomeMessage(user, userAgent);

        // 주간 미션 가져오기 // TODO: 완성 x
        List<UserHomeWeeklyMissionDto> weeklyMissions = missionService.getWeeklyMissions(today);

        // 오늘의 지출 합계 // TODO: 완성 x
        Long todayExpenseAmount = transactionService.getTotalExpenseForDate(user, today);

        // 최근 지출 내역 (최대 3개)
        List<ExpenseUnitResponseDto> recentExpenses = transactionService.getRecentExpenses(user, 3);

        // 주간 예산 정보 // TODO: 완성 x
        UserHomeWeeklyBudgetDto weeklyBudget = monthlyBudgetService.getWeeklyBudgetInfo(user, today);

        return UserHomeResponseDto.builder()
                .welcomeMessage(welcomeMessage)
                .agentImgPath(userAgent.getAgent().getImgPath())
                .weeklyMissions(weeklyMissions)
                .todayExpenseAmount(todayExpenseAmount)
                .recentExpenses(recentExpenses)
                .weeklyBudget(weeklyBudget)
                .build();
    }

    private String generateWelcomeMessage(User user, UserAgent userAgent) {
        String nickname = user.getNickname();
        Agent agent = userAgent.getAgent();

        return switch (agent.getAgentName()) {
            case "무니" -> String.format("좋은 아침이에요 %s님!\n오늘도 예산을 철저히 관리해봐요!", nickname);
            case "써니" -> String.format("안녕하세요 %s님!\n오늘도 알차고 멋진 소비를 해봐요!", nickname);
            case "티타" -> String.format("좋은 아침입니다, %s님.\n오늘도 철저한 절약, 잊지 않으셨죠?", nickname);
            case "에피" -> String.format("안녕하세요, %s님.\n오늘 하루도 나를 위한 따뜻한 소비를 해봐요!", nickname);
            default -> String.format("좋은 아침이에요, %s님!\n오늘도 현명한 소비를 해봐요!", nickname);
        };
    }
}
