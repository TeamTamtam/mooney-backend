package tamtam.mooney.domain.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.agent.entity.UserAgent;
import tamtam.mooney.domain.agent.repository.UserAgentRepository;
import tamtam.mooney.domain.budget.entity.CategoryBudget;
import tamtam.mooney.domain.budget.entity.MonthlyBudget;
import tamtam.mooney.domain.budget.service.CategoryBudgetService;
import tamtam.mooney.domain.budget.service.MonthlyBudgetService;
import tamtam.mooney.domain.chat.dto.ChatBudgetInfoDto;
import tamtam.mooney.domain.chat.dto.ChatRequestDto;
import tamtam.mooney.domain.chat.dto.ChatResponseDto;
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
    private final UserAgentRepository userAgentRepository;
    private final OpenAIService openAIService;
    private final MonthlyBudgetService monthlyBudgetService;
    private final CategoryBudgetService categoryBudgetService;
    private final TransactionService transactionService;

    public ChatResponseDto chat(ChatRequestDto requestDto) {
        User user = userService.getCurrentUser();
        List<ChatBudgetInfoDto> budgetInfoDtos = getCategoryBudgetRemainingAmount(user);
        StringBuilder stringBuilder = new StringBuilder();
        for (ChatBudgetInfoDto dto : budgetInfoDtos) {
            stringBuilder.append(String.format("- %s: %d원 남음\n", dto.categoryName(), dto.remaining()));
        }
        return new ChatResponseDto(generateGPTResponseForChat(user, requestDto.message(), stringBuilder.toString()));
    }

    @Transactional(readOnly = true)
    public List<ChatBudgetInfoDto> getCategoryBudgetRemainingAmount(User user) {
        // 이번 월의 카테고리별 예산 조회
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        MonthlyBudget monthlyBudget = monthlyBudgetService.getMonthlyBudget(user, startOfMonth);
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
//    private String generateScenarioBudgetAnalysisPrompt() {
//        return """
//    **반드시 정확히 상황을 판단하고 예산 카테고리를 명확히 파악해 응답하세요!**
//
//    사용자의 메시지를 분석하여 다음 절차를 순서대로 수행하세요:
//
//    1단계. [상황 판단]
//    - 사용자가 명확히 두 가지 이상의 상품(서비스 포함) 중에서 고민 중이라면 → `CHOICE_RECOMMENDATION`
//    - 사용자가 하나의 상품(서비스 포함)을 구매할지 여부만을 고민 중이라면 → `YES_NO_DECISION`
//
//    2단계. [예산 카테고리 판단]
//    - 사용자가 구매를 고민하는 상품이 어떤 소비 카테고리에 포함되는지 정확히 판단하세요.
//    - 소비 카테고리는 [예산 카테고리별 남은 금액]에 언급됩니다. (ex. 쇼핑, 식비)
//    - 판단할 수 없으면 '기타'로 분류하세요.
//
//    3단계. [남은 예산과 상품 가격 분석]
//    - 판단한 예산 카테고리의 남은 예산 금액을 확인하세요.
//    - 상품의 가격은 사용자 메시지에 상품의 정확한 가격이 있으면 이를 활용하고, 없다면 일반적이고 합리적인 가격을 가정하세요.
//    - 상품의 가격과 남은 예산을 비교하여 구매 가능 여부를 판단하세요.
//        사용자의 남은 예산을 고려하여 두 상품 모두 예산 범위 내에서 구매할 수 있더라도 아직 이번달이 많이 남았다면,
//        앞으로 지출이 생길 가능성을 추론하여 비교적 고가의 상품보다는 상대적으로 합리적인 가격의 상품을 선택하도록 도움
//    2단계에서 파악한 예산 카테고리를 답변 시 명시적으로 언급하세요. (예: "쇼핑 예산", "문화생활 예산")
//
//    2단계의 결과에 따라 다음 예시 답변 형식을 따르세요:
//
//    [답변 예시]
//    - 예산 범위 내에서 모두 구매 가능하지만 고가의 상품과 저렴한 상품 간의 고민이 있는 경우:
//      "[이름]님, 현재 쇼핑 예산은 70,000원이 남아 있어서 A와 B 모두 선택 가능해요. 다만, B는 A에 비해 더 합리적인 가격이라 예산 관리 측면에서는 좋은 선택일 수 있어요. 이번엔 조금 절약하고, 다음 기회에 A를 선택하시는 것도 좋은 방법이에요. 어떤 결정을 하시든 항상 응원할게요!"
//    - 예산은 충분하지만 가성비에 대한 고민이 있는 경우:
//      "[이름]님, 현재 문화생활 예산은 30,000원이 남아 있어서 구매 가능해요. 다만 가격 대비 효용성을 중요하게 생각하시는 것 같으니, 가성비 좋은 다른 옵션을 고려하시는 것도 좋아요. 물론 이 상품이 너무 마음에 드신다면 자신을 위한 소소한 선물로 구매하셔도 괜찮답니다!"
//    - 예산 초과로 구매가 불가능할 것으로 생각되는 경우:
//      "남은 예산으로는 조금 어려워 보여요.😢 더 합리적인 대안을 추천해드릴게요."
//      충동성 지출을 절제하는 것이 장기적으로 더 만족스러운 결정이라는 점을 다정하고 설득력 있게 전달.
//
//    답변은 항상 친절하고 다정하게 사용자의 현명한 소비 의사를 응원하는 어조로 작성하세요.
//    """;
//        }

    // 상황 판단 프롬프트 (GPT가 CHOICE_RECOMMENDATION 또는 YES_NO_DECISION 판단)
    private String generateScenarioPrompt() {
        return """
    **정확하게 상황을 판단하세요!**
    사용자의 메시지를 분석해 아래 중 정확한 상황을 판단:

    - **CHOICE_RECOMMENDATION**: 사용자가 두 개 이상의 상품(A vs B)을 비교하여 하나를 선택하려는 경우.
      사용자의 남은 예산을 고려하여 두 상품 모두 예산 범위 내에서 구매할 수 있더라도 아직 이번달이 많이 남았다면,
      앞으로 지출이 생길 가능성을 추론하여 비교적 고가의 상품보다는 상대적으로 합리적인 가격의 상품을 선택하도록 도움
    - **YES_NO_DECISION**: 사용자가 특정 상품 하나(혹은 여러 상품을 묶어서)를 살지 말지 결정하려는 경우.

    **주의**:
    - 사용자가 특정 상품을 단독으로 언급했어도, 명확한 다른 상품과의 비교가 없다면 YES_NO_DECISION으로 판단하세요.
    """;
    }

    private String generateBudgetAnalysisPrompt() {
        return """
    **중요: 예산 분석과 구매 가능성 평가**

    1. **예산 카테고리 판단**
        - 사용자의 메시지에서 상품의 키워드를 분석해, 상품별로 관련있는 예산 카테고리를 매핑.
        - 예산 카테고리 종류는 [예산 카테고리별 남은 금액]에 언급됨. (ex. 상품이 음식->식비, 상품이 옷->쇼핑)
        - 관련 카테고리가 없으면 "기타"로 분류.

    2. **남은 예산 확인**
        - 해당 예산 카테고리의 현재 남은 예산 금액을 사용자에게 안내.

    3. **상품 가격 분석**
        - 사용자가 가격을 명시했다면 해당 가격을, 그렇지 않다면 일반적인 시장 가격을 가정.

    4. **구매 가능성 평가 및 추천 답변**
        **예산 내 충분히 구매 가능**:
        - "충분히 예산 내에서 구매 가능해요! 😊 하지만 다른 지출 계획도 고려해보세요."

        **예산 내 가능하지만 부담되는 경우**:
        - "예산이 빠듯할 수 있으니 신중히 결정하거나, 절약할 수 있는 대안을 고려해보는 것도 좋아요."

        **예산 초과로 불가능**:
        - "남은 예산으로는 조금 어려워 보여요.😢 더 합리적인 대안을 추천해드릴게요."
        - 충동성 지출을 절제하는 것이 장기적으로 더 만족스러운 결정이라는 점을 다정하고 설득력 있게 전달.
    """;
    }

    // GPT가 참고할 사용자 예시 응답
    private String generateSampleResponse(String userNickname) {
        return String.format(
                """
                [상황 예시] 쇼핑 카테고리 예산이 70,000원 남은 상황을 가정
                [상황별 응답 예시]
                - **if CHOICE_RECOMMENDATION** (구체적 상품명을 반드시 언급):
                  "%s님, 현재 쇼핑 예산은 70,000원이 남았어요. \n\
                  나이키 운동화와 아디다스 운동화 모두 예산 내에서 가능하지만 😊, 아디다스 제품이 더 저렴하고 부담이 적어요. 앞으로의 지출 계획까지 고려해 선택하는 게 현명할 것 같아요!"
                - **if YES_NO_DECISION** (구체적 상품명을 반드시 언급):
                  "%s님, 쇼핑 예산 70,000원으로 무신사 티셔츠는 구매 가능해요. \n\
                  다만, 다른 필수 지출도 생각해보고 결정하면 좋겠어요!"
                
                [적절한 응답 분량]:
                {사용자}님, 현재 남은 예산 내에서 고민하시는 두 상품 모두 충분히 선택할 수 있어요. 다만 {A}쪽이 {B}보다 조금 더 고급스러워서 예산 관리 측면에서 살짝 고민될 수 있을 것 같아요. 내심 더 고가의 {A}이 끌리시지만, 조금 참고 상대적으로 합리적인 {B}로 선택의 균형을 잡으려 하시는 것 같네요.
                이럴 때는 가격도 합리적이고, 만족감이나 실용성{B의 장점}에서도 절대 부족함 없는 {B}을 선택하시면 장기적으로 더 뿌듯하실 거예요. 다음에 더 고급스러운 상품을 선택할 때의 즐거움이 더 특별해질지도 모르죠! 😊 어떤 결정을 하시든 현명한 소비를 응원합니다!
                """,
                userNickname, userNickname
        );
    }

    @Transactional(readOnly = true)
    public String generateGPTResponseForChat(User user, String userMessage, String budgetInfo) {
        UserAgent userAgent = userAgentRepository.findByUserAndIsActiveTrue(user)
                .orElseThrow(() -> new IllegalArgumentException("No active UserAgent found."));

        String agentPrompt = openAIService.generateUserAgentPrompt(userAgent);
        String finalInstruction = openAIService.generateFinalInstruction(userAgent);

        String scenarioPrompt = generateScenarioPrompt();
        String budgetAnalysisPrompt = generateBudgetAnalysisPrompt();
        String sampleResponse = generateSampleResponse(user.getNickname());

        LocalDate today = LocalDate.now();

        String message = String.format(
                """
                %s
    
                %s
    
                %s
    
                [예산 카테고리별 남은 금액]:
                %s
    
                %s
                ---
                [오늘 날짜]: %s
                [사용자 %s의 메시지]: "%s"
    
                %s
    
                **응답할 때 반드시 구체적인 상품명과 예산 카테고리를 명확히 언급하여 답변을 작성해주세요!**
                """,
                agentPrompt,
                scenarioPrompt,
                budgetAnalysisPrompt,
                budgetInfo,
                sampleResponse,
                today,
                user.getNickname(),
                userMessage,
                finalInstruction
        );
        return openAIService.generateGPTResponse(message, OpenAIOptionEnum.LOGICAL);
    }
}
