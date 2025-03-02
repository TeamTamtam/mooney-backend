package tamtam.mooney.domain.transaction.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record DailyTransactionResponseDto(
        LocalDate date,
        Long totalIncomeAmount,
        Long totalExpenseAmount,
        List<IncomeUnitResponseDto> incomes,
        List<ExpenseUnitResponseDto> expenses
) {
    public static DailyTransactionResponseDto from(
            LocalDate date, Long totalIncomeAmount, Long totalExpenseAmount,
            List<ExpenseUnitResponseDto> expenses, List<IncomeUnitResponseDto> incomes) {
        return DailyTransactionResponseDto.builder()
                .date(date)
                .totalIncomeAmount(totalIncomeAmount)
                .totalExpenseAmount(totalExpenseAmount)
                .incomes(incomes)
                .expenses(expenses)
                .build();
    }
}
