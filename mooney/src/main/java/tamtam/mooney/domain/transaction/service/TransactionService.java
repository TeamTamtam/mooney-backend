package tamtam.mooney.domain.transaction.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.transaction.dto.*;
import tamtam.mooney.domain.transaction.entity.Expense;
import tamtam.mooney.domain.transaction.entity.Income;
import tamtam.mooney.domain.transaction.entity.Transaction;
import tamtam.mooney.domain.transaction.repository.ExpenseRepository;
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
    private final ExpenseRepository expenseRepository;
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
                        expense.getTransactionSource(),
                        expense.getNote(),
                        expense.getPayee(),
                        expense.getExpenseCategory()
                ));
                totalExpenseAmount += expense.getAmount();
            } else if (transaction instanceof Income income) {
                incomes.add(new IncomeUnitResponseDto(
                        income.getTransactionId(),
                        income.getAmount(),
                        income.getTransactionTime(),
                        income.getTransactionSource(),
                        income.getNote(),
                        income.getPayer()
                ));
                totalIncomeAmount += income.getAmount();
            }
        }

        return MonthlyTransactionDayUnitDto.from(date, totalIncomeAmount, totalExpenseAmount, expenses, incomes);
    }

    @Transactional(readOnly = true)
    public MonthlyTransactionResponseDto getTransactionsByMonth(int year, int month) {
        User user = userService.getCurrentUser();

        // 선택한 월의 시작일과 마지막일 계산
        LocalDate startOfMonth = LocalDate.of(year, month, 1);
        LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

        // 해당 월의 전체 지출 및 수입 합계 계산
        List<Object[]> results1 = transactionRepository.getTotalIncomeAndExpenseByMonth(user, startOfMonth.atStartOfDay(), endOfMonth.atTime(23, 59, 59));
        long totalIncomeAmount = 0L, totalExpenseAmount = 0L;
        for (Object[] result : results1) {
            totalIncomeAmount += ((Number) result[0]).longValue();
            totalExpenseAmount += ((Number) result[1]).longValue();
        }

        // 해당 월의 시작일부터 끝일까지의 모든 날짜를 Map에 초기화
        Map<LocalDate, DailyTransactionSummaryDto> summaryMap = new LinkedHashMap<>();
        for (LocalDate date = startOfMonth; !date.isAfter(endOfMonth); date = date.plusDays(1)) {
            summaryMap.put(date, new DailyTransactionSummaryDto(date, 0L, 0L)); // 기본값 0
        }

        // DB에서 조회한 결과 반영
        List<Object[]> results = transactionRepository.getDailyTotalIncomeAndExpenseByMonth(user, startOfMonth.atStartOfDay(), endOfMonth.atTime(23, 59, 59));
        for (Object[] result : results) {
            LocalDate date = (LocalDate) result[0];
            long dailyIncome = ((Number) result[1]).longValue();
            long dailyExpense = ((Number) result[2]).longValue();
            summaryMap.put(date, new DailyTransactionSummaryDto(date, dailyIncome, dailyExpense));
        }

        // Map을 List로 변환하여 DTO 반환
        List<DailyTransactionSummaryDto> dailySummaries = new ArrayList<>(summaryMap.values());

        return MonthlyTransactionResponseDto.builder()
                .totalIncomeAmount(totalIncomeAmount)
                .totalExpenseAmount(totalExpenseAmount)
                .dailySummaries(dailySummaries)
                .build();
    }

    // 특정 날짜의 총 지출 금액 조회
    @Transactional(readOnly = true)
    public Long getTotalExpenseForDate(User user, LocalDate date) {
        return transactionRepository.getDailyTotalExpenseAmount(user, date.atStartOfDay(), date.atTime(23, 59, 59));
    }

    // 최근 지출 내역을 최대 limit개 조회
    @Transactional(readOnly = true)
    public List<ExpenseUnitResponseDto> getRecentExpenses(User user, int limit) {
        return expenseRepository.findRecentExpenses(user, limit).stream()
                .map(expense -> new ExpenseUnitResponseDto(
                        expense.getTransactionId(),
                        expense.getAmount(),
                        expense.getTransactionTime(),
                        expense.getTransactionSource(),
                        expense.getNote(),
                        expense.getPayee(),
                        expense.getExpenseCategory()
                ))
                .toList();
    }
}