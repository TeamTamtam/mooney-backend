package tamtam.mooney.domain.notification.service;

import java.util.Set;

public class NotificationTypeValidator {
    private static final Set<String> NOTIFICATION_TYPES = Set.of(
            // 정기 알림 (Scheduled)
            "MONTHLY_FEEDBACK",
            "MONTHLY_NEW_BUDGET",
            "WEEKLY_NEW_MISSION",
            "DAILY_SPENDING_SUMMARY",

            // 예산 초과 관련 알림 (Budget)
            "CATEGORY_BUDGET_EXCEEDED",
            "TOTAL_BUDGET_EXCEEDED",

            // 사용자 동기부여를 위한 알림
            "INACTIVE_USER_REMINDER",  // 일정 기간 가계부 미사용 시 알림
            "MISSION_COMPLETED",
            "BUDGETING_TIPS"
    );

    public static boolean isValid(String type) {
        return NOTIFICATION_TYPES.contains(type);
    }

    public static Set<String> getAllNotificationTypes() {
        return NOTIFICATION_TYPES;
    }
}
