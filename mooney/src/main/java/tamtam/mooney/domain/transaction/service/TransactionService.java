package tamtam.mooney.domain.transaction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.transaction.dto.*;
import tamtam.mooney.domain.transaction.entity.Expense;
import tamtam.mooney.domain.transaction.entity.Income;
import tamtam.mooney.domain.transaction.entity.Transaction;
import tamtam.mooney.domain.transaction.repository.TransactionRepository;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.domain.user.service.UserService;

import java.time.LocalDate;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public DailyTransactionResponseDto getTransactionsByDate(LocalDate date) {
        User user = userService.getCurrentUser();

        // 하나의 쿼리로 Expense와 Income 모두 조회
        List<Transaction> transactions = transactionRepository.findByUserAndTransactionTimeBetweenOrderByTransactionTime(
                user, date.atStartOfDay(), date.plusDays(1).atStartOfDay());

        // DTO로 변환
        List<ExpenseUnitResponseDto> expenses = new ArrayList<>();
        List<IncomeUnitResponseDto> incomes = new ArrayList<>();
        long totalExpenseAmount = 0L;
        long totalIncomeAmount = 0L;

        for (Transaction transaction : transactions) {
            if (transaction instanceof Expense expense) {
                expenses.add(new ExpenseUnitResponseDto(
                        expense.getTransactionId(),
                        expense.getAmount(),
                        expense.getTransactionTime(),
                        expense.getExpenseCategory(),
                        expense.getTransactionSource(),
                        expense.getNote()
                ));
                totalExpenseAmount += expense.getAmount();
            } else if (transaction instanceof Income income) {
                incomes.add(new IncomeUnitResponseDto(
                        income.getTransactionId(),
                        income.getAmount(),
                        income.getTransactionTime(),
                        income.getTransactionSource(),
                        income.getNote()
                ));
                totalIncomeAmount += income.getAmount();
            }
        }

        return DailyTransactionResponseDto.from(date, totalIncomeAmount, totalExpenseAmount, expenses, incomes);
    }

}