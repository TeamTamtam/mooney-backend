package tamtam.mooney.domain.transaction.dto;

import lombok.Builder;
import tamtam.mooney.domain.transaction.entity.IncomeCategory;
import java.time.LocalDateTime;

@Builder
public record IncomeUnitResponseDto(
        Long incomeId,
        Long amount,
        LocalDateTime transactionTime,
        IncomeCategory incomeCategory,
        String transactionSource,
        String note
) {}
