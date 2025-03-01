package tamtam.mooney.domain.transaction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.transaction.dto.IncomeAddRequestDto;
import tamtam.mooney.domain.transaction.entity.Income;
import tamtam.mooney.domain.transaction.entity.IncomeCategory;
import tamtam.mooney.domain.transaction.repository.IncomeRepository;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.domain.user.service.UserService;

@Service
@Transactional
@RequiredArgsConstructor
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final UserService userService;
    // private final LlmCategoryClassifier llmCategoryClassifier;

    // 수입 추가
    public String createIncome(IncomeAddRequestDto request) {
        User user = userService.getCurrentUser();

        // String predictedCategory = llmCategoryClassifier.classifyCategory(request.payer(), false);
        String predictedCategory = IncomeCategory.SALARY.name();
        IncomeCategory incomeCategory = IncomeCategory.valueOf(predictedCategory);

        Income income = Income.builder()
                .payer(request.payer())
                .incomeCategory(incomeCategory)
                .amount(request.amount())
                .transactionDate(request.transactionTime())
                .transactionSource(request.transactionSource())
                .sourceApp(request.sourceApp())
                .user(user)
                .build();

        incomeRepository.save(income);
        return income.getIncomeCategory().name();
    }

}
