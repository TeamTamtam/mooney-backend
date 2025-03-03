package tamtam.mooney.domain.transaction.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RecurringTransactionDto(
        @NotBlank
        @Size(min = 1)
        String title,
        long amount,
        String period
) {}