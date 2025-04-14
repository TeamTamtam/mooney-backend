package tamtam.mooney.domain.transaction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.mission.repository.MissionRepository;
import tamtam.mooney.domain.mission.service.MissionService;
import tamtam.mooney.domain.transaction.dto.ExpenseAddRequestDto;
import tamtam.mooney.domain.transaction.entity.Expense;
import tamtam.mooney.domain.enums.ExpenseCategory;
import tamtam.mooney.domain.transaction.repository.TransactionRepository;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.domain.user.service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@Transactional
@RequiredArgsConstructor
public class ExpenseService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;
    // private final LlmCategoryClassifier llmCategoryClassifier;
    private final MissionRepository missionRepository;
    private final MissionService missionService;

    // 지출 추가
    public String createExpense(ExpenseAddRequestDto request) {
        User user = userService.getCurrentUser();

        // String predictedCategory = llmCategoryClassifier.classifyCategory(request.payee(), true);
        String predictedCategory = ExpenseCategory.FOOD.name();
        ExpenseCategory expenseCategory = ExpenseCategory.valueOf(predictedCategory);

        Expense expense = Expense.builder()
                .payee(request.payee())
                .expenseCategory(expenseCategory)
                .amount(request.amount())
                .transactionTime(request.transactionTime())
                .transactionSource(request.transactionSource())
                .sourceApp(request.sourceApp())
                .user(user)
                .build();

        transactionRepository.save(expense);

        //들어온 지출의 payee가 현재 진행중인 미션 place에 해당된다면 missionRepo에 save
        missionService.updateMission(user, request.payee(),request.amount());

        return expense.getExpenseCategory().name();
    }

    @Transactional(readOnly = true)
    public Long getTotalExpenseAmountForPeriod(User user, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        return transactionRepository.getTotalExpenseAmountForPeriod(user, startDateTime, endDateTime);
    }
}
