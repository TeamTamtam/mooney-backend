package tamtam.mooney.domain.transaction.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record IncomeUnitResponseDto(
        Long transactionId,
        Long amount,
        LocalDateTime transactionTime,
        String transactionSource,
        String note
) {}
