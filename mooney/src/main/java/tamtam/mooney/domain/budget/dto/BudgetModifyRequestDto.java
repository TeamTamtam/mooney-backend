package tamtam.mooney.domain.budget.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

import java.util.List;

@Builder
public record BudgetModifyRequestDto (
        int year,
        int month,
        @NotNull
        Long monthlyBudgetAmount,
        List<CategoryBudgetSimpleUnitDto> categoryBudgets
) {}
