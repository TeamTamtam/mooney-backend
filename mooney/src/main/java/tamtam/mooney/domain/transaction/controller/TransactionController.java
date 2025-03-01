package tamtam.mooney.domain.transaction.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tamtam.mooney.domain.transaction.dto.DailyTransactionResponseDto;
import tamtam.mooney.domain.transaction.dto.ExpenseAddRequestDto;
import tamtam.mooney.domain.transaction.dto.IncomeAddRequestDto;
import tamtam.mooney.domain.transaction.service.ExpenseService;
import tamtam.mooney.domain.transaction.service.IncomeService;
import tamtam.mooney.domain.transaction.service.TransactionService;

import java.time.LocalDate;

@Tag(name = "Transaction")
@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;
    private final ExpenseService expenseService;
    private final IncomeService incomeService;

    @Operation(summary = "지출 내역 추가")
    @PostMapping("/expenses")
    public ResponseEntity<String> createExpense(@RequestBody @Valid ExpenseAddRequestDto request) {
        String category = expenseService.createExpense(request);
        return ResponseEntity.ok(category);
    }

    @Operation(summary = "수입 내역 추가")
    @PostMapping("/incomes")
    public ResponseEntity<String> createIncome(@RequestBody @Valid IncomeAddRequestDto request) {
        incomeService.createIncome(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "사용자가 요청한 날짜의 수입 및 지출 내역 조회")
    @GetMapping("/daily")
    public ResponseEntity<DailyTransactionResponseDto> getTransactionsByDate(@RequestParam("date") @NotNull LocalDate date) {
        return ResponseEntity.ok(transactionService.getTransactionsByDate(date));
    }
}
