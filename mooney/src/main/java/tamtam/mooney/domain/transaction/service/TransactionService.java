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
import java.util.*;

@Service
@Transactional
@RequiredArgsConstructor
public class TransactionService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final UserService userService;

    // 한 날짜의 내역 조회 (오름차순 정렬 + 총 지출 & 수입 추가)
    @Transactional(readOnly = true)
    public DailyTransactionResponseDto getTransactionsByDate(LocalDate date) {
        User user = userService.getCurrentUser();

        // 사용자의 수입 및 지출 데이터 가져오기 (오름차순 정렬)
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
                        .incomeCategory(income.getIncomeCategory())
                        .transactionSource(income.getTransactionSource())
                        .note(income.getNote())
                        .build())
                .toList();

        // 총 지출 & 총 수입 계산
        Long totalExpenseAmount = expenses.stream()
                .mapToLong(ExpenseUnitResponseDto::amount)
                .sum();

        Long totalIncomeAmount = incomes.stream()
                .mapToLong(IncomeUnitResponseDto::amount)
                .sum();

        // DTO 생성하여 반환
        return DailyTransactionResponseDto.builder()
                .date(date)
                .totalExpenseAmount(totalExpenseAmount)
                .totalIncomeAmount(totalIncomeAmount)
                .expenses(expenses)
                .incomes(incomes)
                .build();
    }
}