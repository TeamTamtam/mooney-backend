package tamtam.mooney.global.openai;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.stereotype.Service;

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
}
