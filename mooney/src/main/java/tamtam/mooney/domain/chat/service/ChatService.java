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
import tamtam.mooney.domain.chat.dto.ChatMessage;
import tamtam.mooney.domain.chat.dto.ChatRequestDto;
import tamtam.mooney.domain.chat.dto.ChatResponseDto;
import tamtam.mooney.domain.enums.ExpenseCategory;
import tamtam.mooney.domain.transaction.service.ExpenseService;
import tamtam.mooney.domain.transaction.service.TransactionService;
import tamtam.mooney.domain.user.entity.User;
import tamtam.mooney.domain.user.service.UserService;
import tamtam.mooney.global.openai.OpenAIOptionEnum;
import tamtam.mooney.global.openai.OpenAIService;
import tamtam.mooney.global.redis.GenericRedisRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
    private final ExpenseService expenseService;
    private final GenericRedisRepository<ChatMessage> chatRedisRepository;

    // 채팅 저장
    public void saveChatMessage(String userId, String message, String role) {
        ChatMessage chatMessage = new ChatMessage(
                UUID.randomUUID().toString(),
                userId,
                role,
                message,
                LocalDateTime.now()
        );
        chatRedisRepository.save("chat:" + userId, chatMessage);
    }


    @Transactional(readOnly = true)
    public List<ChatMessage> getChatHistory(String userId) {
        return chatRedisRepository.findAll("chat:" + userId);
    }


    public ChatResponseDto chat(ChatRequestDto requestDto) {
        User user = userService.getCurrentUser();

        // 예산 정보 조회
        List<ChatBudgetInfoDto> budgetInfoDtos = getCategoryBudgetRemainingAmount(user);
        String budgetInfoString = budgetInfoDtos.stream()
                .map(dto -> String.format("- %s: %d원 남음\n", dto.categoryName(), dto.remaining()))
                .collect(Collectors.joining());

        // GPT 응답 생성
        String gptResponse = generateGPTResponseForChat(user, requestDto.message(), budgetInfoString);

        // Redis에 채팅 내역 저장
        saveChatMessage(user.getUserId().toString(), requestDto.message(), "USER");
        saveChatMessage(user.getUserId().toString(), gptResponse, "GPT");

        return new ChatResponseDto(gptResponse);
    }

    @Transactional(readOnly = true)
    public List<ChatBudgetInfoDto> getCategoryBudgetRemainingAmount(User user) {
        // 이번 월의 카테고리별 예산 조회
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        MonthlyBudget monthlyBudget = monthlyBudgetService.getMonthlyBudget(user, startOfMonth);
        List<CategoryBudget> budgets = categoryBudgetService.findByMonthlyBudget(monthlyBudget);

        // 특정 기간 동안의 모든 카테고리별 총 지출
        Map<ExpenseCategory, Long> totalExpensesByCategory = expenseService.mapTotalExpenseForAllCategories(user, startOfMonth);

        // 각 카테고리의 실제 지출 계산
        return budgets.stream()
                .map(cb -> {
                    Long spent = totalExpensesByCategory.getOrDefault(cb.getExpenseCategory(), 0L);
                    long remaining = Math.max(cb.getAmount() - spent, 0);

                    return new ChatBudgetInfoDto(
                            cb.getExpenseCategory().getCategoryName(),
                            remaining
                    );
                }).collect(Collectors.toList());
    }

    // 상황 판단 프롬프트 (GPT가 CHOICE_RECOMMENDATION 또는 YES_NO_DECISION 판단)
    private String generateScenarioPrompt() {
        return """
    ## 1. 상황 판단
    - **CHOICE_RECOMMENDATION**: 두 개 이상 상품 비교 요청 (상품 수 무관)
      - 여러 상품이 예산 내에 있다면, 남은 기간 추가 지출을 고려해 더 합리적인 선택 제안
    
    - **YES_NO_DECISION**: 특정 상품 구매 여부 결정 요청
    """;
    }

    private String generateBudgetAnalysisPrompt() {
        return """
    ## 2. 예산 분석과 구매 가능성 평가
        1) 메시지에서 상품별 관련 예산 카테고리를 추출. 예산 카테고리 종류는 [예산 카테고리별 남은 금액]에 언급됨. (ex. 상품이 음식->식비) (없으면 기타)
        2)  **[예산 카테고리별 남은 금액]**에서 해당 카테고리 잔여 금액 안내
        3) 제시 가격 또는 시장가 가정
        4) 지출 비율 계산(ex. 2만 원 = 잔여 예산의 50%)
        5) 과거 지출 내역 기반 남은 이번 달 소비 경향 추론
           [과거 지출 요약] 블록의 통계치를 참고해,
           - 다음 기간 예상 지출 규모
           - 주요 지출 패턴(빈도·증가·감소 추이)
           등을 추론해서 반영
        6) 남은 기간과 날짜 고려해 최종 소비 가능 여부·추천 옵션 산출
    
    ## 3. 답변 구성
    - **충분히 가능**
        - 구체적 수치(금액)로 설명
        - 기대 효과 언급 (만족감, 편리함)
    - **부담되는 경우**
        - 현재 부담 정도 설명
        - 대안 제시
        - 미래 소비 예측 반영
    - **불가능할 때**
        - 친절하고 설득력 있게 공감
        - 충동 지출 자제 권유
        - 감성적 동기 부여 (“다음에 더 맛있게…” 등)
    - **출력 구조**
        1) 비교·수치 분석 설명 단락 \s
        2) 최종 추천 + 감성 동기부여 단락
    ## 4. 톤 & 포맷
        - 숫자를 정확하게
        - 이모지 1~2개로 친근함 추가
        - 마크다운 사용 금지
        - 문장 끝은 모두 '~요'체로 마무리
    """;
    }

    // GPT가 참고할 사용자 예시 응답
    private String generateSampleResponse(String userNickname) {
        return String.format(
                """
                [적절한 응답 분량]: 250~350자 내외
                [상황별 응답 예시]
                    - **if CHOICE_RECOMMENDATION** (구체적 상품명을 반드시 언급):
                        "%s님, 현재 식비 예산이 30,000원 남아있어요. 고기(20,000원)는 잔여 예산의 67%%를 차지해요. 조금 큰 비중이라, 하루 식사로 지출하기엔 조금 부담스러울 수 있어요. 컵밥(3,000원)을 선택하시면 다음 주 예상 식비 지출(적어도 4~5만 원 이상 예상)에 대한 부담이 훨씬 줄어들 거예요.\\n가끔은 작은 절약이 더 큰 만족으로 돌아와요. 오늘은 컵밥으로 아끼고, 고기는 다음에 조금 더 여유 있을 때 드시면 더 맛있고 기분 좋게 드실 수 있을 거라 생각해요!😊"
                    - **if YES_NO_DECISION** (구체적 상품명을 반드시 언급):
                        "%s님, 쇼핑 예산이 50,000원 남아있어요. 100,000원짜리 옷을 사면 예산을 50,000원 초과하게 됩니다. 아직 한 달이 많이 남았으니, 이번 달 다른 지출을 최소화해 예산을 재조정하면 가능할 수도 있어요.\\n예를 들어, 외식비나 카페비에서 50,000원만 절약하면 오늘 옷 구매가 부담 없이 이루어질 거예요. 😊 그렇지만, 조금 더 여유를 모아 다음 달 초에 구매하는 것도 한 방법이에요. %s님이 기분 좋게 쇼핑하실 수 있도록 응원할게요!"
                """,
                userNickname, userNickname, userNickname
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
//        String expenseInfo = """
//            - 식비: 월평균 400,000원, 주 3회 지출
//            - 쇼핑: 월평균 80,000원, 월 2회 지출
//            - 교통: 월평균 70,000원, 주 5회 이용
//        """;
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
                [오늘]: %s
                [사용자 %s]: "%s"
    
                %s
    
                **응답할 때 반드시 구체적인 상품명과 예산 카테고리를 명확히 언급하여 답변을 작성해주세요!**
                """,
                agentPrompt,
                scenarioPrompt,
                budgetAnalysisPrompt,
                budgetInfo,
//                expenseInfo,
                sampleResponse,
                today,
                user.getNickname(),
                userMessage,
                finalInstruction
        );
        return openAIService.generateGPTResponse(message, OpenAIOptionEnum.BALANCED);
    }
}
