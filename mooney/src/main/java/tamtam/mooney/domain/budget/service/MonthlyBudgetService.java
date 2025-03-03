package tamtam.mooney.domain.budget.service;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.budget.dto.CategoryBudgetProgressUnitDto;
import tamtam.mooney.domain.budget.dto.FirstBudgetRequestDto;
import tamtam.mooney.domain.budget.dto.BudgetProgressResponseDto;
import tamtam.mooney.domain.budget.entity.CategoryBudget;
import tamtam.mooney.domain.budget.entity.MonthlyBudget;
import tamtam.mooney.domain.budget.repository.MonthlyBudgetRepository;
import tamtam.mooney.domain.transaction.service.ExpenseService;
import tamtam.mooney.domain.transaction.service.RecurringTransactionService;
import tamtam.mooney.domain.transaction.service.ScheduledTransactionService;
import tamtam.mooney.domain.transaction.service.TransactionService;
import tamtam.mooney.domain.user.dto.UserHomeWeeklyBudgetDto;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.domain.user.service.UserService;
import tamtam.mooney.global.exception.CustomException;
import tamtam.mooney.global.exception.ErrorCode;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class MonthlyBudgetService {
    private final MonthlyBudgetRepository monthlyBudgetRepository;
    private final CategoryBudgetService categoryBudgetService;
    private final TransactionService transactionService;

    @Transactional(readOnly = true)
    public MonthlyBudget getMonthlyBudget(User user, LocalDate monthDate) {
        return monthlyBudgetRepository.findByUserAndMonthDate(user, monthDate)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public UserHomeWeeklyBudgetDto getWeeklyBudgetInfo(User user, LocalDate today) {
        return null;
    }

    public Object getMonthlyBudgetPlan(@NotNull @Min(1900) int year, @NotNull @Min(1) @Max(12) int month) {
    }
}
