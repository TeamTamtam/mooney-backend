package tamtam.mooney.domain.enums;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public enum ExpenseCategory {
    FOOD(1, "식비", "🍽️"),
    CAFE_SNACKS(2, "카페/간식", "🍩"),
    ALCOHOL_ENTERTAINMENT(3, "술/유흥", "🍻"),
    LIVING(4, "생활", "🛒"),
    ONLINE_SHOPPING(5, "온라인쇼핑", "📦"),
    FASHION_SHOPPING(6, "패션/쇼핑", "👗"),
    BEAUTY_CARE(7, "뷰티/미용", "💄"),
    TRANSPORTATION(8, "교통", "🚊"),
    CAR(9, "자동차", "🚗"),
    HOUSING_COMMUNICATION(10, "주거/통신", "🏡"),
    HEALTHCARE(11, "의료/건강", "🏥"),
    FINANCE(12, "금융", "💰"),
    CULTURE_LEISURE(13, "문화/여가", "🎧"),
    TRAVEL_ACCOMMODATION(14, "여행/숙박", "✈️"),
    EDUCATION(15, "교육/학습", "📚"),
    CHILDCARE(16, "자녀/육아", "👶"),
    PET(17, "반려동물", "🐶"),
    GIFT_CEREMONY(18, "경조/선물", "🎁"),
    OTHER(19, "이체", "↔️"); // 그 외

    @NotNull
    private final int code;  // 숫자 값 추가
    @NotNull
    private final String categoryName;
    @NotNull
    private final String icon;

    ExpenseCategory(int code, String categoryName, String icon) {
        this.code = code;
        this.categoryName = categoryName;
        this.icon = icon;
    }

    // 숫자로 ENUM 변환 (DB에서 불러올 때 사용)
    public static ExpenseCategory fromCode(int code) {
        for (ExpenseCategory category : ExpenseCategory.values()) {
            if (category.getCode() == code) {
                return category;
            }
        }
        throw new IllegalArgumentException("Invalid ExpenseCategory code: " + code);
    }
}
