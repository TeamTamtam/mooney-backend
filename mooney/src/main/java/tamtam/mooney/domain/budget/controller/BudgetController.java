package tamtam.mooney.domain.budget.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tamtam.mooney.domain.budget.dto.FirstBudgetRequestDto;
import tamtam.mooney.domain.budget.dto.MonthlyBudgetProgressResponseDto;
import tamtam.mooney.domain.budget.service.MonthlyBudgetService;

import java.time.LocalDate;

@Tag(name = "Budget")
@RestController
@RequiredArgsConstructor
@RequestMapping("/budgets")
public class BudgetController {
    private final MonthlyBudgetService monthlyBudgetService;

    @Operation(summary = "첫 예산 수립")
    @PostMapping("/first-budget")
    public ResponseEntity<?> saveFirstBudget(@RequestBody @Valid FirstBudgetRequestDto firstBudgetRequestDto) {
        monthlyBudgetService.saveFirstBudget(firstBudgetRequestDto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "월별 예산 조회")
    @GetMapping("/monthly")
    public ResponseEntity<MonthlyBudgetProgressResponseDto> getMonthlyBudgetProgress(
            @RequestParam @NotNull @Min(1900) int year,
            @RequestParam @NotNull @Min(1) @Max(12) int month,
            @RequestParam @NotNull LocalDate today) {
        return ResponseEntity.ok(monthlyBudgetService.getMonthlyBudgetProgress(year, month, today));
    }
}
