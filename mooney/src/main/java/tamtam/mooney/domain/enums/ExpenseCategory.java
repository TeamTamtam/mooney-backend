package tamtam.mooney.domain.enums;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
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
    OTHER(19, "기타", "↔️"); // 그 외

    @NotNull
    private final int code;  // 숫자 값 추가
    @NotNull
    private final String categoryName;
    @NotNull
    private final String icon;

    private static final Map<Integer, ExpenseCategory> BY_CODE =
            Arrays.stream(values()).collect(Collectors.toMap(ExpenseCategory::getCode, Function.identity()));
    private static final Map<String, ExpenseCategory> BY_NAME =
            Arrays.stream(values()).collect(Collectors.toMap(c -> c.categoryName, Function.identity()));

    // 숫자로 ENUM 변환
    public static ExpenseCategory fromCode(int code) {
        return BY_CODE.getOrDefault(code, OTHER);
    }

    // 한글 카테고리명으로 ENUM 변환
    public static ExpenseCategory fromCategoryName(String name) {
        if (name == null) return OTHER;
        return BY_NAME.getOrDefault(name.trim(), OTHER);
    }
}
