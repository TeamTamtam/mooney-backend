package tamtam.mooney.domain.transaction.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.transaction.repository.IncomeRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class IncomeService {
    private final IncomeRepository incomeRepository;
}
