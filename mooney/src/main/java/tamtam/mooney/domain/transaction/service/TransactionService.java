package tamtam.mooney.domain.transaction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.transaction.dto.ExpenseAddRequestDto;
import tamtam.mooney.domain.transaction.dto.IncomeAddRequestDto;
import tamtam.mooney.domain.transaction.entity.Expense;
import tamtam.mooney.domain.transaction.entity.ExpenseCategory;
import tamtam.mooney.domain.transaction.entity.Income;
import tamtam.mooney.domain.transaction.entity.IncomeCategory;
import tamtam.mooney.domain.transaction.repository.ExpenseRepository;
import tamtam.mooney.domain.transaction.repository.IncomeRepository;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.domain.user.service.UserService;

@Service
@Transactional
@RequiredArgsConstructor
public class TransactionService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final UserService userService;
    private final LlmCategoryClassifier llmCategoryClassifier;

    // 지출(Expense) 저장 API
    public String createExpense(ExpenseAddRequestDto request) {
        User user = userService.getCurrentUser();

        // String predictedCategory = llmCategoryClassifier.classifyCategory(request.payee(), true);
        String predictedCategory = ExpenseCategory.FOOD.name();
        ExpenseCategory expenseCategory = ExpenseCategory.valueOf(predictedCategory);

        Expense expense = Expense.builder()
                .payee(request.payee())
                .paymentMethod(request.paymentMethod())
                .expenseCategory(expenseCategory)
                .amount(request.amount())
                .transactionDate(request.transactionTime())
                .transactionSource(request.transactionSource())
                .sourceApp(request.sourceApp())
                .user(user)
                .build();

        expenseRepository.save(expense);
        return expense.getExpenseCategory().name();
    }

    // 수입(Income) 저장 API
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
