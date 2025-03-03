package tamtam.mooney.domain.transaction.dto;

import lombok.Builder;
import java.util.List;

@Builder
public record MonthlyTransactionResponseDto(
        Long totalIncomeAmount,
        Long totalExpenseAmount,
        List<DailyTransactionSummaryDto> dailySummaries // 전체 월의 날짜별 수입 & 지출 합계
) {
    public static MonthlyTransactionResponseDto from(Long totalIncomeAmount, Long totalExpenseAmount, List<DailyTransactionSummaryDto> dailySummaries) {
        return MonthlyTransactionResponseDto.builder()
                .totalIncomeAmount(totalIncomeAmount)
                .totalExpenseAmount(totalExpenseAmount)
                .dailySummaries(dailySummaries)
                .build();
    }
}
