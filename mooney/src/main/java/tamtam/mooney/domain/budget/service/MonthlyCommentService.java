package tamtam.mooney.domain.budget.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.budget.repository.MonthlyCommentRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class MonthlyCommentService {
    private final MonthlyCommentRepository monthlyCommentRepository;
}
