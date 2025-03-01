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
) {}
