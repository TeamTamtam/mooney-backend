package tamtam.mooney.domain.transaction.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tamtam.mooney.domain.transaction.dto.*;
import tamtam.mooney.domain.transaction.service.ExpenseDataLoader;
import tamtam.mooney.domain.transaction.service.ExpenseService;
import tamtam.mooney.domain.transaction.service.IncomeService;
import tamtam.mooney.domain.transaction.service.TransactionService;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Tag(name = "Transaction")
@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;
    private final ExpenseService expenseService;
    private final IncomeService incomeService;
    private final ExpenseDataLoader expenseDataLoader;

    @Operation(summary = "지출 내역 추가")
    @PostMapping("/expenses")
    public ResponseEntity<String> createExpense(@RequestBody @Valid ExpenseAddRequestDto request) {
        String category = expenseService.createExpense(request);
        return ResponseEntity.ok(category);
    }

    @Operation(summary = "지출 내역 여러 건 추가")
    @PostMapping("/expenses-multiple")
    public ResponseEntity<Void> createMultipleExpenses(
            @RequestBody @Valid List<ExpenseAddRequestDto> requests) {
        // 각 DTO마다 서비스 호출
        for (ExpenseAddRequestDto dto : requests) {
            expenseService.createExpense(dto);
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "수입 내역 추가")
    @PostMapping("/incomes")
    public ResponseEntity<String> createIncome(@RequestBody @Valid IncomeAddRequestDto request) {
        incomeService.createIncome(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "특정 날짜의 수입 및 지출 내역 조회")
    @GetMapping("/daily")
    public ResponseEntity<MonthlyTransactionDayUnitDto> getTransactionsByDate(@RequestParam @NotNull LocalDate date) {
        return ResponseEntity.ok(transactionService.getTransactionsByDate(date));
    }


    @Operation(summary = "특정 월의 수입 및 지출 내역 조회")
    @GetMapping("/monthly")
    public ResponseEntity<MonthlyTransactionResponseDto> getTransactionsByMonth(@RequestParam @NotNull @Min(2024) int year,
                                                                                @RequestParam @NotNull @Min(1) @Max(12) int month) {
        return ResponseEntity.ok(transactionService.getTransactionsByMonth(year, month));
    }

}
