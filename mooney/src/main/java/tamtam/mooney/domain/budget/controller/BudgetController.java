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
import tamtam.mooney.domain.budget.dto.BudgetModifyRequestDto;
import tamtam.mooney.domain.budget.dto.BudgetProgressResponseDto;
import tamtam.mooney.domain.budget.service.BudgetService;

import java.time.LocalDate;

@Tag(name = "Budget")
@RestController
@RequiredArgsConstructor
@RequestMapping("/budgets")
public class BudgetController {
    private final BudgetService budgetService;

    @Operation(summary = "첫 예산 수립")
    @PostMapping("/first-budget")
    public ResponseEntity<?> saveFirstBudget(@RequestBody @Valid FirstBudgetRequestDto requestDto) {
        budgetService.saveFirstBudget(requestDto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "특정 월의 예산 계획 조회")
    @GetMapping("/plan")
    public ResponseEntity<?> getBudgetPlan(@RequestParam @NotNull @Min(2024) int year,
                                           @RequestParam @NotNull @Min(1) @Max(12) int month) {
        return ResponseEntity.ok(budgetService.getBudgetPlan(year, month));
    }

    @Operation(summary = "특정 월의 예산 계획 수정")
    @PatchMapping("/plan")
    public ResponseEntity<?> modifyBudgetPlan(@RequestBody @Valid BudgetModifyRequestDto requestDto) {
        budgetService.modifyBudgetPlan(requestDto);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "특정 월의 예산 진행 조회")
    @GetMapping("/progress")
    public ResponseEntity<BudgetProgressResponseDto> getBudgetProgress(@RequestParam @NotNull @Min(2024) int year,
                                                                       @RequestParam @NotNull @Min(1) @Max(12) int month,
                                                                       @RequestParam @NotNull LocalDate today) {
        return ResponseEntity.ok(budgetService.getBudgetProgress(year, month, today));
    }
}
