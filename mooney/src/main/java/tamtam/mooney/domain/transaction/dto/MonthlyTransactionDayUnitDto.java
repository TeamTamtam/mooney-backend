package tamtam.mooney.domain.transaction.dto;

import lombok.Builder;

import java.time.LocalDate;
import java.util.List;

@Builder
public record MonthlyTransactionDayUnitDto(
        LocalDate date,
        Long totalIncomeAmount,
        Long totalExpenseAmount,
        List<IncomeUnitResponseDto> incomes,
        List<ExpenseUnitResponseDto> expenses
) {
    public static MonthlyTransactionDayUnitDto from(
            LocalDate date, Long totalIncomeAmount, Long totalExpenseAmount,
            List<ExpenseUnitResponseDto> expenses, List<IncomeUnitResponseDto> incomes) {
        return MonthlyTransactionDayUnitDto.builder()
                .date(date)
                .totalIncomeAmount(totalIncomeAmount)
                .totalExpenseAmount(totalExpenseAmount)
                .incomes(incomes)
                .expenses(expenses)
                .build();
    }
}
