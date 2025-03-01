package tamtam.mooney.domain.transaction.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tamtam.mooney.domain.transaction.dto.ExpenseAddRequestDto;
import tamtam.mooney.domain.transaction.dto.IncomeAddRequestDto;
import tamtam.mooney.domain.transaction.service.TransactionService;

@Tag(name = "Transaction")
@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {
    private final TransactionService transactionService;

    @Operation(summary = "지출 내역 추가")
    @PostMapping("/expenses")
    public ResponseEntity<String> createExpense(@RequestBody @Valid ExpenseAddRequestDto request) {
        String category = transactionService.createExpense(request);
        return ResponseEntity.ok(category);
    }

    @Operation(summary = "수입 내역 추가")
    @PostMapping("/incomes")
    public ResponseEntity<String> createIncome(@RequestBody @Valid IncomeAddRequestDto request) {
        String category = transactionService.createIncome(request);
        return ResponseEntity.ok(category);
    }
}
