package tamtam.mooney.domain.budget.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RecurringTransactionDto(
        @NotBlank
        @Size(min = 1)
        String title,
        @NotNull
        Long amount,
        String period
) {}