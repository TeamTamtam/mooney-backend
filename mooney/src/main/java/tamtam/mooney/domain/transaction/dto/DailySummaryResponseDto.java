package tamtam.mooney.domain.transaction.dto;

import lombok.Builder;
import java.time.LocalDate;

@Builder
public record DailySummaryResponseDto(
        LocalDate date,
        Long totalIncomeAmount,
        Long totalExpenseAmount
) {}
