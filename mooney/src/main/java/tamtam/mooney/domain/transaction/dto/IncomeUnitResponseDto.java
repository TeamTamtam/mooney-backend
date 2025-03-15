package tamtam.mooney.domain.transaction.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record IncomeUnitResponseDto(
        long transactionId,
        long amount,
        LocalDateTime transactionTime,
        String transactionSource,
        String note,
        String payer
) {}
