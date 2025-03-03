package tamtam.mooney.domain.transaction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.transaction.dto.ScheduledTransactionResponseDto;
import tamtam.mooney.domain.transaction.entity.ScheduledTransaction;
import tamtam.mooney.domain.transaction.repository.ScheduledTransactionRepository;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.domain.user.service.UserService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class ScheduledTransactionService {

    private final ScheduledTransactionRepository scheduledTransactionRepository;
    private final UserService userService;

    // 특정 월의 ScheduledTransaction 정보를 조회
    public List<ScheduledTransactionResponseDto> getScheduledTransactionsByMonth(User user, int year, int month) {
        LocalDate startOfMonth = YearMonth.of(year, month).atDay(1);
        LocalDate endOfMonth = YearMonth.of(year, month).atEndOfMonth();

        List<ScheduledTransaction> scheduledTransactions = scheduledTransactionRepository.findByUserAndScheduledDateBetween(user, startOfMonth, endOfMonth);

        return scheduledTransactions.stream()
                .map(ScheduledTransactionResponseDto::from)
                .collect(Collectors.toList());
    }

    public Long getTotalScheduledTransactionAmountByMonth(User user, int year, int month) {
        LocalDate startOfMonth = YearMonth.of(year, month).atDay(1);
        LocalDate endOfMonth = YearMonth.of(year, month).atEndOfMonth();
        return scheduledTransactionRepository.getTotalAmountByUserAndScheduledDateBetween(user, startOfMonth, endOfMonth);
    }

    public Long getTotalPendingScheduledTransactionAmountByMonth(User user, int year, int month) {
        LocalDate startOfMonth = YearMonth.of(year, month).atDay(1);
        LocalDate endOfMonth = YearMonth.of(year, month).atEndOfMonth();
        return scheduledTransactionRepository.getTotalAmountByUserAndTransactionIsNullAndScheduledDateBetween(user, startOfMonth, endOfMonth);
    }
}
