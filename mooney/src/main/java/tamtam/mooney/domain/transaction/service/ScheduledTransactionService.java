package tamtam.mooney.domain.transaction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.transaction.dto.ScheduledTransactionResponseDto;
import tamtam.mooney.domain.transaction.entity.ScheduledTransaction;
import tamtam.mooney.domain.transaction.repository.ScheduledTransactionRepository;
import tamtam.mooney.domain.user.entity.User;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ScheduledTransactionService {

    private final ScheduledTransactionRepository scheduledTransactionRepository;

//    // ScheduledTransactionResponseDto 응딥
//    @Transactional(readOnly = true)
//    public List<ScheduledTransactionResponseDto> getScheduledTransactionResponseDtoByMonth(User user, LocalDate startOfMonth, LocalDate endOfMonth) {
//        List<ScheduledTransaction> scheduledTransactions = getScheduledTransactionsByMonth(user, startOfMonth, endOfMonth);
//        return scheduledTransactions.stream()
//                .map(ScheduledTransactionResponseDto::from)
//                .collect(Collectors.toList());
//    }

    // 특정 월의 지출+저축 ScheduledTransaction 조회
    @Transactional(readOnly = true)
    public List<ScheduledTransaction> getScheduledExpensesByMonth(User user, LocalDate startOfMonth, LocalDate endOfMonth) {
        return scheduledTransactionRepository.findByUserAndScheduledDateBetweenAndTransactionType(user, startOfMonth, endOfMonth, "EXPENSE");
    }

    // 특정 월의 모든 ScheduledTransaction 조회
    @Transactional(readOnly = true)
    public List<ScheduledTransaction> getScheduledTransactionsForMonth(User user, LocalDate startOfMonth, LocalDate endOfMonth) {
        return scheduledTransactionRepository.getScheduledTransactionsForMonth(user, startOfMonth, endOfMonth);
    }

    // 특정 월의 예정된 ScheduledTransaction 조회
    @Transactional(readOnly = true)
    public Long getTotalPendingScheduledTransactionAmountByMonth(User user, LocalDate startOfMonth, LocalDate endOfMonth) {
        return scheduledTransactionRepository.getTotalAmountByUserAndTransactionIsNullAndScheduledDateBetween(user, startOfMonth, endOfMonth);
    }

    /*@Transactional(readOnly = true)
    public Long getTotalScheduledTransactionAmountByMonth(User user, int year, int month) {
        LocalDate startOfMonth = YearMonth.of(year, month).atDay(1);
        LocalDate endOfMonth = YearMonth.of(year, month).atEndOfMonth();
        return scheduledTransactionRepository.getTotalAmountByUserAndScheduledDateBetween(user, startOfMonth, endOfMonth);
    }*/
}
