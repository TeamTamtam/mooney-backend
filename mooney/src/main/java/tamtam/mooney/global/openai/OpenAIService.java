package tamtam.mooney.global.openai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tamtam.mooney.domain.agent.entity.Agent;
import tamtam.mooney.domain.agent.entity.UserAgent;
import tamtam.mooney.domain.agent.repository.UserAgentRepository;
import tamtam.mooney.domain.user.entity.User;

@Service
@RequiredArgsConstructor
public class OpenAIService {
    private final OpenAiChatModel openAiChatModel;
    private final UserAgentRepository userAgentRepository;

    private String generateBasePrompt(UserAgent userAgent) {
        return String.format(
                """
                너는 "%s"야. %s의 성격, %s한 어조를 가진 금융 어시스턴트야.
                항상 이 성격과 말투를 유지하며 대답해야 해.
                """,
                userAgent.getAgent().getAgentName(),
                userAgent.getAgent().getPersonality(),
                userAgent.getAgentTone()
        );
    }
    private String generateFinalInstruction(UserAgent userAgent) {
        return String.format(
                "이제 %s의 개성을 반영하여 자연스럽게 답변을 생성해.",
                userAgent.getAgent().getAgentName()
        );
    }

    @Transactional(readOnly = true)
    public String generateGPTResponseForChat(User user, String userMessage, String budgetInfo) {
        // 현재 활성화된 UserAgent 조회
        UserAgent userAgent = userAgentRepository.findByUserAndIsActiveTrue(user)
                .orElseThrow(() -> new IllegalArgumentException("No active UserAgent found."));

        Agent agent = userAgent.getAgent(); // 유저가 선택한 캐릭터

        String budgetAnalysisInstruction = """
        **중요:** 사용자의 메시지를 분석하여 어떤 예산 카테고리와 관련이 있는지 반드시 판단해야 합니다.
        사용자의 메시지에서 키워드를 추출하여, 아래 제공된 예산 카테고리와 매칭하세요.
        관련 있는 예산 카테고리를 찾지 못하면 "기타"로 분류하세요.
        """;

        String scenarioInstruction = """
        **반드시 정확히 상황을 판단해야 합니다!**
        사용자의 메시지를 분석하여 아래 두 가지 중 하나를 선택하세요:
        - CHOICE_RECOMMENDATION: 두 개의 선택지(A vs B) 중 하나를 고르는 경우.
        - YES_NO_DECISION: 특정 아이템(A)을 구매할지 말지 결정하는 경우.
        """;

        String sampleResponse = """
        if 상황 == CHOICE_RECOMMENDATION:
        화연님의 쇼핑 카테고리 예산은 현재 70,000원이 남아 있어요.
        A를 사는 것도 예산 내에서는 가능해요 😊 하지만 B는 훨씬 저렴하고 예산 부담이 적어요.
        이번 달이 아직 많이 남았으니, 앞으로 있을 다른 쇼핑 계획도 고려해보는 게 좋아요.
        if 상황 == YES_NO_DECISION:
        화연님의 쇼핑 예산은 70,000원이 남아 있지만, A의 가격이 예산을 초과하지 않는지 확인해보세요.
        만약 예산을 초과한다면, 다음 달 예산을 미리 조정할 수 있을지 고려해보는 것도 좋은 방법이에요.
        현재 다른 필수 지출 계획이 있다면, 우선순위를 정해서 신중하게 결정해보세요!
        """;

        String systemInstruction = String.format(
                """
                %s
    
                %s
                
                %s
    
                [예산 카테고리별 남은 금액]:
                %s
    
                [예시(이것을 베이스로 %s의 말투를 새롭게 적용해야 함)]:
                %s
    
                [사용자의 질문]: "%s"
    
                %s
                """,
                generateBasePrompt(userAgent),
                scenarioInstruction, // GPT가 상황을 판단하도록 지시
                budgetAnalysisInstruction, // GPT가 관련된 예산 카테고리를 식별하도록 지시
                budgetInfo, // 예산 정보 포함
                agent.getAgentName(),
                sampleResponse,
                userMessage,
                generateFinalInstruction(userAgent)
        );
        return generateGPTResponse(systemInstruction, OpenAIOptionEnum.valueOf("LOGICAL"));
    }

    public String generateGPTResponse(String message, OpenAIOptionEnum optionType) {
        Prompt prompt = new Prompt(
                new UserMessage(message),
                optionType.toChatOptions() // 옵션 전달
        );
        return openAiChatModel.call(prompt).getResult().getOutput().getText();
    }
}
