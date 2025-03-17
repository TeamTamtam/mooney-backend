package tamtam.mooney.global.openai;

import lombok.Getter;
import org.springframework.ai.openai.OpenAiChatOptions;

@Getter
public enum OpenAIOptionEnum {
    CREATIVE("gpt-4o-mini", 0.9, 700, 1.0, 0.0, 0.0),  // 창의적 답변
    BALANCED("gpt-4o-mini", 0.7, 500, 0.8, 0.0, 0.0),  // 균형 잡힌 답변
    LOGICAL("gpt-4o-mini", 0.2, 500, 0.8, 0.0, -0.5); // 논리적인 답변

    private final String model;
    private final double temperature;
    private final int maxTokens;
    private final double topP;
    private final double frequencyPenalty;
    private final double presencePenalty;

    OpenAIOptionEnum(String model, double temperature, int maxTokens, double topP, double frequencyPenalty, double presencePenalty) {
        this.model = model;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.topP = topP;
        this.frequencyPenalty = frequencyPenalty;
        this.presencePenalty = presencePenalty;
    }

    // OpenAiChatOptions 객체로 변환
    public OpenAiChatOptions toChatOptions() {
        return OpenAiChatOptions.builder()
                .model(this.model)
                .temperature(this.temperature)
                .maxTokens(this.maxTokens)
                .topP(this.topP)
                .frequencyPenalty(this.frequencyPenalty)
                .presencePenalty(this.presencePenalty)
                .build();
    }
}