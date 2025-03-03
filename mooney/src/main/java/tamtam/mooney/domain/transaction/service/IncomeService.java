package tamtam.mooney.domain.transaction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.transaction.dto.IncomeAddRequestDto;
import tamtam.mooney.domain.transaction.entity.Income;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.domain.user.service.UserService;

@Service
@Transactional
@RequiredArgsConstructor
public class IncomeService {

    private final IncomeRepository incomeRepository;
    private final UserService userService;

    // 수입 추가
    public void createIncome(IncomeAddRequestDto request) {
        User user = userService.getCurrentUser();

        Income income = Income.builder()
                .payer(request.payer())
                .amount(request.amount())
                .transactionDate(request.transactionTime())
                .transactionSource(request.transactionSource())
                .sourceApp(request.sourceApp())
                .user(user)
                .build();

        incomeRepository.save(income);
    }
}
