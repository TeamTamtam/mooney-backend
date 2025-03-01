package tamtam.mooney.domain.budget.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tamtam.mooney.domain.budget.dto.FirstBudgetRequestDto;
import tamtam.mooney.domain.budget.service.MonthlyBudgetService;

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
}
