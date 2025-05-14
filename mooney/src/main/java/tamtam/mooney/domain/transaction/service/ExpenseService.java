package tamtam.mooney.domain.transaction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.mission.service.MissionService;
import tamtam.mooney.domain.transaction.dto.BaseExpenseRequest;
import tamtam.mooney.domain.transaction.dto.ExpenseAddRequestDto;
import tamtam.mooney.domain.transaction.dto.ExpenseWithCategoryAddRequestDto;
import tamtam.mooney.domain.transaction.entity.Expense;
import tamtam.mooney.domain.enums.ExpenseCategory;
import tamtam.mooney.domain.transaction.repository.ExpenseRepository;
import tamtam.mooney.domain.transaction.repository.TransactionRepository;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.domain.user.service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ExpenseService {

    private final TransactionRepository transactionRepository;
    private final ExpenseRepository expenseRepository;
    private final UserService userService;
    // private final LlmCategoryClassifier llmCategoryClassifier;
    private final MissionService missionService;

    // 지출 추가 (카테고리 예측)
    public String createExpense(ExpenseAddRequestDto request) {
        User user = userService.getCurrentUser();
        String predicted = "FOOD"; //llmCategoryClassifier.classifyCategory(request.payee(), true);
        ExpenseCategory category = ExpenseCategory.valueOf(predicted);
        return saveAndReturnCategory(request, user, category);
    }

    // 지출 추가 (카테고리 직접 지정)
    public String createExpenseWithCategory(ExpenseWithCategoryAddRequestDto request) {
        User user = userService.getCurrentUser();
        return saveAndReturnCategory(request, user, request.expenseCategory());
    }

    // ——— private 헬퍼 메서드 ———
    private <T extends BaseExpenseRequest> String saveAndReturnCategory(
            T request,
            User user,
            ExpenseCategory category
    ) {
        Expense expense = Expense.builder()
                .payee(request.payee())
                .expenseCategory(category)
                .amount(request.amount())
                .transactionTime(request.transactionTime())
                .transactionSource(request.transactionSource())
                .sourceApp(request.sourceApp())
                .user(user)
                .build();

        transactionRepository.save(expense);
        missionService.updateMission(user, request.payee(), request.amount());
        return category.name();
    }

    @Transactional(readOnly = true)
    public Long getTotalExpenseAmountForPeriod(User user, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.atTime(23, 59, 59);
        return transactionRepository.getTotalExpenseAmountForPeriod(user, startDateTime, endDateTime);
    }

    @Transactional(readOnly = true)
    public Map<ExpenseCategory, Long> mapTotalExpenseForAllCategories(User user, LocalDate startDate) {
        LocalDateTime startOfMonth = startDate.atStartOfDay();
        LocalDateTime endOfMonth = startDate.withDayOfMonth(startDate.lengthOfMonth()).atTime(23, 59, 59);
        log.info("startOfMonth: " + startOfMonth + " / endOfMonth: " + endOfMonth);

        // List<Object[]>로 쿼리 결과 받기
        List<Object[]> rawResults = expenseRepository.getTotalExpenseForAllCategories(user, startOfMonth, endOfMonth);

        // 수동 변환: Map<ExpenseCategory, Long>
        Map<ExpenseCategory, Long> result = new HashMap<>();
        for (Object[] row : rawResults) {
            ExpenseCategory category = (ExpenseCategory) row[0];
            Long amount = (Long) row[1];
            result.put(category, amount);
        }

        // 전체 결과 출력
        result.forEach((key, value) ->
                log.info("카테고리: " + key.name() + " / 총 지출: " + value)
        );

        return result;
    }


}
