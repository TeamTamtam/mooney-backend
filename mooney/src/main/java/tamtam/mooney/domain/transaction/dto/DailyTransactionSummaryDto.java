package tamtam.mooney.domain.transaction.dto;

import lombok.Builder;
import java.time.LocalDate;

@Builder
public record DailyTransactionSummaryDto(
        LocalDate date,
        long totalIncomeAmount,
        long totalExpenseAmount
) {}
