package tamtam.mooney.domain.budget.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.budget.entity.MonthlyBudget;
import tamtam.mooney.domain.budget.repository.MonthlyBudgetRepository;
import tamtam.mooney.domain.user.dto.UserHomeWeeklyBudgetDto;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.global.exception.CustomException;
import tamtam.mooney.global.exception.ErrorCode;

import java.time.LocalDate;

@Service
@Transactional
@RequiredArgsConstructor
public class MonthlyBudgetService {
    private final MonthlyBudgetRepository monthlyBudgetRepository;

    @Transactional(readOnly = true)
    public MonthlyBudget getMonthlyBudget(User user, LocalDate startOfMonth) {
        return monthlyBudgetRepository.findByUserAndMonthDate(user, startOfMonth)
                .orElseThrow(() -> new CustomException(ErrorCode.RESOURCE_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public UserHomeWeeklyBudgetDto getWeeklyBudgetInfo(User user, LocalDate today) {
        return null;
    }
}
