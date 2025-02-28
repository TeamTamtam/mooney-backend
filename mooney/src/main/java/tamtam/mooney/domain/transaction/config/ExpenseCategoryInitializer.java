package tamtam.mooney.domain.transaction.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.transaction.entity.ExpenseCategory;
import tamtam.mooney.domain.transaction.repository.ExpenseCategoryRepository;

import java.util.Set;

@Component
@RequiredArgsConstructor
public class ExpenseCategoryInitializer {

    private final ExpenseCategoryRepository expenseCategoryRepository;

    @PostConstruct
    @Transactional
    public void initExpenseCategories() {
        if (expenseCategoryRepository.count() > 0) return; // 이미 데이터가 있으면 종료

        Set.of(
                "FOOD,🍽️,식비",
                "CAFE_SNACKS,🍩,카페/간식",
                "ALCOHOL_ENTERTAINMENT,🍻,술/유흥",
                "LIVING,🏠,생활",
                "ONLINE_SHOPPING,🛒,온라인쇼핑",
                "FASHION_SHOPPING,👗,패션/쇼핑",
                "BEAUTY_CARE,💄,뷰티/미용",
                "TRANSPORTATION,🚕,교통",
                "CAR,🚗,자동차",
                "HOUSING_COMMUNICATION,📶,주거/통신",
                "HEALTHCARE,🏥,의료/건강",
                "FINANCE,💰,금융",
                "CULTURE_LEISURE,🎭,문화/여가",
                "TRAVEL_ACCOMMODATION,✈️,여행/숙박",
                "EDUCATION,📚,교육/학습",
                "CHILDCARE,👶,자녀/육아",
                "PET,🐶,반려동물",
                "GIFT_CEREMONY,🎁,경조/선물"
        ).forEach(category -> {
            String[] parts = category.split(",");
            expenseCategoryRepository.save(new ExpenseCategory(parts[2], parts[1]));
        });

        System.out.println("Expense Categories initialized.");
    }
}
