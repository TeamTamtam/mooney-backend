package tamtam.mooney.domain.transaction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.transaction.dto.*;
import tamtam.mooney.domain.transaction.entity.Expense;
import tamtam.mooney.domain.transaction.entity.ExpenseCategory;
import tamtam.mooney.domain.transaction.entity.Income;
import tamtam.mooney.domain.transaction.entity.Transaction;
import tamtam.mooney.domain.transaction.repository.TransactionRepository;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.domain.user.service.UserService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public MonthlyTransactionDayUnitDto getTransactionsByDate(LocalDate date) {
        User user = userService.getCurrentUser();

        // 특정 날짜의 Transactions(Expense와 Income) 모두 조회
        List<Transaction> transactions = transactionRepository.findByUserAndTransactionTimeBetweenOrderByTransactionTime(
                user, date.atStartOfDay(), date.plusDays(1).atStartOfDay());

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

        return MonthlyTransactionDayUnitDto.from(date, totalIncomeAmount, totalExpenseAmount, expenses, incomes);
    }

    public Long getTotalExpenseForCategory(User user, ExpenseCategory expenseCategory, LocalDate startDate, LocalDate endDate) {
        LocalDateTime startOfMonth = startDate.atStartOfDay();
        LocalDateTime endOfMonth = endDate.atTime(23, 59, 59);
        return transactionRepository.getTotalExpenseForCategory(user, expenseCategory, startOfMonth, endOfMonth);
    }

    @Transactional(readOnly = true)
    public MonthlyTransactionResponseDto getTransactionsByMonth(int year, int month) {
        User user = userService.getCurrentUser();

        // 선택한 월의 시작일과 마지막일 계산
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        // 해당 월의 전체 지출 및 수입 합계 계산
        Long totalIncomeAmount = transactionRepository.getTotalIncomeAmountForMonth(user, startOfMonth.atStartOfDay(), endOfMonth.atTime(23, 59, 59));
        Long totalExpenseAmount = transactionRepository.getTotalExpenseAmountForMonth(user, startOfMonth.atStartOfDay(), endOfMonth.atTime(23, 59, 59));

        // 해당 월의 모든 날짜별 지출 및 수입 합계 조회
        List<DailyTransactionSummaryDto> dailySummaries = new ArrayList<>();
        for (LocalDate date = startOfMonth; !date.isAfter(endOfMonth); date = date.plusDays(1)) {
            Long dailyIncome = transactionRepository.getTotalIncomeAmountForDate(user, date.atStartOfDay(), date.plusDays(1).atStartOfDay());
            Long dailyExpense = transactionRepository.getTotalExpenseAmountForDate(user, date.atStartOfDay(), date.plusDays(1).atStartOfDay());

            dailySummaries.add(new DailyTransactionSummaryDto(date, dailyIncome, dailyExpense));
        }

        return MonthlyTransactionResponseDto.builder()
                .totalIncomeAmount(totalIncomeAmount)
                .totalExpenseAmount(totalExpenseAmount)
                .dailySummaries(dailySummaries)
                .build();
    }
}