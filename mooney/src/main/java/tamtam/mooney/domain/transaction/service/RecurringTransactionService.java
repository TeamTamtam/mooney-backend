package tamtam.mooney.domain.transaction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.transaction.repository.RecurringTransactionRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class RecurringTransactionService {
    private final RecurringTransactionRepository recurringTransactionRepository;
}
