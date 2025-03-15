package tamtam.mooney.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.budget.entity.CategoryBudget;
import tamtam.mooney.domain.budget.entity.MonthlyBudget;
import tamtam.mooney.domain.budget.service.CategoryBudgetService;
import tamtam.mooney.domain.budget.service.MonthlyBudgetService;
import tamtam.mooney.domain.chat.dto.ChatBudgetInfoDto;
import tamtam.mooney.domain.chat.dto.ChatRequestDto;
import tamtam.mooney.domain.chat.repository.ChatRepository;
import tamtam.mooney.domain.transaction.service.TransactionService;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.domain.user.service.UserService;
import tamtam.mooney.global.openai.OpenAIOptionEnum;
import tamtam.mooney.global.openai.OpenAIService;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class ChatService {
    private final UserService userService;
    private final OpenAIService openAIService;
    private final MonthlyBudgetService monthlyBudgetService;
    private final CategoryBudgetService categoryBudgetService;
    private final TransactionService transactionService;

    public String chat(ChatRequestDto requestDto) {
        User user = userService.getCurrentUser();
        List<ChatBudgetInfoDto> budgetInfoDtos = getCategoryBudgetRemainingAmount(user);
        return openAIService.generateGPTResponseForChat(user, requestDto.message(), formatBudgetInfo(budgetInfoDtos));
    }

    private String formatBudgetInfo(List<ChatBudgetInfoDto> budgetInfoDtos) {
        StringBuilder budgetInfo = new StringBuilder();
        for (ChatBudgetInfoDto dto : budgetInfoDtos) {
            budgetInfo.append(String.format("- %s: %d원 남음\n", dto.categoryName(), dto.remaining()));
        }
        return budgetInfo.toString();
    }

    @Transactional(readOnly = true)
    public List<ChatBudgetInfoDto> getCategoryBudgetRemainingAmount(User user) {
        // 이번 월의 카테고리별 예산 조회
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        MonthlyBudget monthlyBudget = monthlyBudgetService.getMonthlyBudget(user, LocalDate.now());
        List<CategoryBudget> budgets = categoryBudgetService.findByMonthlyBudget(monthlyBudget);

        // 특정 기간 동안의 모든 카테고리별 총 지출
        Map<String, Long> totalExpensesByCategory = transactionService.mapTotalExpenseForAllCategories(user, startOfMonth);

        // 각 카테고리의 실제 지출 계산
        return budgets.stream()
                .map(cb -> {
                    Long spent = totalExpensesByCategory.getOrDefault(cb.getExpenseCategory().name(), 0L);
                    long remaining = Math.max(cb.getAmount() - spent, 0);

                    return new ChatBudgetInfoDto(
                            cb.getExpenseCategory().getCategoryName(),
                            remaining
                    );
                }).collect(Collectors.toList());
    }
}
