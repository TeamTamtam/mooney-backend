package tamtam.mooney.domain.notification.entity;

import java.util.Set;

public class NotificationTypeValidator {
    private static final Set<String> VALID_TYPES = Set.of(
            "SYSTEM", "EVENT", "PROMOTION", "REWARD", "MARKETING", "VIP_OFFER"
    );

    public static boolean isValid(String type) {
        return VALID_TYPES.contains(type);
    }
}
