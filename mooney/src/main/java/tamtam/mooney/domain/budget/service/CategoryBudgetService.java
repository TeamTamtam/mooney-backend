package tamtam.mooney.domain.budget.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.budget.repository.CategoryBudgetRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class CategoryBudgetService {
    private final CategoryBudgetRepository categoryBudgetRepository;
}
