package tamtam.mooney.domain.transaction.dto;

import lombok.Builder;
import tamtam.mooney.domain.transaction.entity.ScheduledTransaction;

import java.time.LocalDate;

@Builder
public record ScheduledTransactionResponseDto(
        long scheduledTransactionId,
        String transactionType,
        String title,
        long amount,
        LocalDate scheduledDate
) {
    public static ScheduledTransactionResponseDto from(ScheduledTransaction scheduledTransaction) {
        return ScheduledTransactionResponseDto.builder()
                .scheduledTransactionId(scheduledTransaction.getScheduledTransactionId())
                .transactionType(scheduledTransaction.getTransactionType())
                .title(scheduledTransaction.getTitle())
                .amount(scheduledTransaction.getAmount())
                .scheduledDate(scheduledTransaction.getScheduledDate())
                .build();
    }
}
