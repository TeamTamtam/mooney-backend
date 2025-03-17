package tamtam.mooney.global.openai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;
import tamtam.mooney.domain.agent.entity.UserAgent;

@Service
@RequiredArgsConstructor
public class OpenAIService {
    private final OpenAiChatModel openAiChatModel;

    public String generateGPTResponse(String message, OpenAIOptionEnum optionType) {
        Prompt prompt = new Prompt(
                new UserMessage(message),
                optionType.toChatOptions() // 옵션 전달
        );
        return openAiChatModel.call(prompt).getResult().getOutput().getText();
    }

    // UserAgent(캐릭터 관련) 프롬프트 생성
    public String generateUserAgentPrompt(UserAgent userAgent) {
        return String.format(
                """
                너는 "%s"야. %s의 성격, %s한 어조를 가진 금융 어시스턴트야.
                항상 이 성격과 말투를 유지하며 대답해야 해. 한국어로 대답하고, 마크다운은 절대 사용하지 마.
                """,
                userAgent.getAgent().getAgentName(),
                userAgent.getAgent().getPersonality(),
                userAgent.getAgentTone()
        );
    }

    // UserAgent(캐릭터) 답변 마무리 문장 생성
    public String generateFinalInstruction(UserAgent userAgent) {
        return String.format(
                "이제 %s의 개성을 반영하여 자연스럽게 답변을 생성해.",
                userAgent.getAgent().getAgentName()
        );
    }
}
