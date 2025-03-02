package tamtam.mooney.domain.transaction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.transaction.dto.*;
import tamtam.mooney.domain.transaction.entity.Expense;
import tamtam.mooney.domain.transaction.entity.Income;
import tamtam.mooney.domain.transaction.repository.ExpenseRepository;
import tamtam.mooney.domain.transaction.repository.IncomeRepository;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.domain.user.service.UserService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class TransactionService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public DailyTransactionResponseDto getTransactionsByDate(LocalDate date) {
        User user = userService.getCurrentUser();

        // 특정 날짜의 수입 및 지출 데이터 가져오기
        List<ExpenseUnitResponseDto> expenses = expenseRepository.findByUserAndTransactionTimeBetween(
                        user, date.atStartOfDay(), date.plusDays(1).atStartOfDay())
                .stream()
                .sorted(Comparator.comparing(Expense::getTransactionTime))
                .map(expense -> ExpenseUnitResponseDto.builder()
                        .expenseId(expense.getExpenseId())
                        .amount(expense.getAmount())
                        .transactionTime(expense.getTransactionTime())
                        .expenseCategory(expense.getExpenseCategory())
                        .transactionSource(expense.getTransactionSource())
                        .note(expense.getNote())
                        .build())
                .toList();
        List<IncomeUnitResponseDto> incomes = incomeRepository.findByUserAndTransactionTimeBetween(
                        user, date.atStartOfDay(), date.plusDays(1).atStartOfDay())
                .stream()
                .sorted(Comparator.comparing(Income::getTransactionTime))
                .map(income -> IncomeUnitResponseDto.builder()
                        .incomeId(income.getIncomeId())
                        .amount(income.getAmount())
                        .transactionTime(income.getTransactionTime())
                        .transactionSource(income.getTransactionSource())
                        .note(income.getNote())
                        .build())
                .toList();

        // 총 지출, 총 수입
        Long totalExpenseAmount = expenses.stream()
                .mapToLong(ExpenseUnitResponseDto::amount)
                .sum();
        Long totalIncomeAmount = incomes.stream()
                .mapToLong(IncomeUnitResponseDto::amount)
                .sum();

        return DailyTransactionResponseDto.from(date, totalIncomeAmount, totalExpenseAmount, expenses, incomes);
    }

    @Transactional(readOnly = true)
    public MonthlyTransactionResponseDto getTransactionsByMonth(int year, int month) {
        User user = userService.getCurrentUser();

        // 해당 월의 시작일, 마지막일 계산
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate firstDay = yearMonth.atDay(1);
        LocalDate lastDay = yearMonth.atEndOfMonth();

        // 사용자의 해당 월 모든 거래 내역 조회
        List<Expense> expenses = expenseRepository.findByUserAndTransactionTimeBetween(
                user, firstDay.atStartOfDay(), lastDay.plusDays(1).atStartOfDay());
        List<Income> incomes = incomeRepository.findByUserAndTransactionTimeBetween(
                user, firstDay.atStartOfDay(), lastDay.plusDays(1).atStartOfDay());

        // 날짜별로 그룹화
        Map<LocalDate, Long> dailyExpenseTotals = expenses.stream()
                .collect(Collectors.groupingBy(
                        expense -> expense.getTransactionTime().toLocalDate(),
                        Collectors.summingLong(Expense::getAmount)
                ));
        Map<LocalDate, Long> dailyIncomeTotals = incomes.stream()
                .collect(Collectors.groupingBy(
                        income -> income.getTransactionTime().toLocalDate(),
                        Collectors.summingLong(Income::getAmount)
                ));

        // 해당 월 전체의 총 수입, 총 지출 계산
        Long totalExpenseAmount = expenses.stream()
                .mapToLong(Expense::getAmount)
                .sum();
        Long totalIncomeAmount = incomes.stream()
                .mapToLong(Income::getAmount)
                .sum();

        // 날짜별로 expense, income 합쳐 리스트 생성
        List<DailySummaryResponseDto> dailySummaries = new ArrayList<>();
        for (LocalDate date = firstDay; !date.isAfter(lastDay); date = date.plusDays(1)) {
            dailySummaries.add(new DailySummaryResponseDto(
                    date,
                    dailyIncomeTotals.getOrDefault(date, 0L),
                    dailyExpenseTotals.getOrDefault(date, 0L)
            ));
        }

        return MonthlyTransactionResponseDto.from(totalIncomeAmount, totalExpenseAmount, dailySummaries);
    }
}