package tamtam.mooney.domain.transaction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.transaction.dto.RecurringTransactionDto;
import tamtam.mooney.domain.transaction.entity.RecurringTransaction;
import tamtam.mooney.domain.transaction.repository.RecurringTransactionRepository;
import tamtam.mooney.domain.user.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class RecurringTransactionService {
    private final RecurringTransactionRepository recurringTransactionRepository;

    public void saveRecurringTransactions(User user, List<RecurringTransactionDto> transactions, String recurringType) {
        Optional.ofNullable(transactions)
                .filter(list -> !list.isEmpty()) // 리스트가 null이 아니고 비어있지 않을 때만 실행
                .ifPresent(list -> {
                    List<RecurringTransaction> recurringTransactions = list.stream()
                            .map(dto -> RecurringTransaction.builder()
                                    .title(dto.title())
                                    .amount(dto.amount())
                                    .period(dto.period())
                                    .recurringType(recurringType)
                                    .user(user)
                                    .build())
                            .collect(Collectors.toList());

                    recurringTransactionRepository.saveAll(recurringTransactions);
                });
    }
}
