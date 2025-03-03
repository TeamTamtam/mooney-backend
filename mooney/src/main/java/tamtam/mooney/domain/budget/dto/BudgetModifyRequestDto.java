package tamtam.mooney.domain.budget.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record BudgetModifyRequestDto (
        @Min(2024)
        int year,
        @Min(1) @Max(12)
        int month,
        @NotNull
        Long monthlyBudgetAmount,
        List<CategoryBudgetSimpleUnitDto> categoryBudgets
) {}
