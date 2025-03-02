package tamtam.mooney.domain.transaction.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class LlmCategoryClassifier {

    @Value("${openai.api.key}")
    private String openAiApiKey;

    private static final String OPENAI_URL = "https://api.openai.com/v1/chat/completions";

    public String classifyCategory(String transactionName, boolean isExpense) {
        String prompt = String.format(
                "\"%s\"는 어떤 카테고리에 속하나요? 가능한 옵션: [%s] 중 하나만 선택해주세요.",
                transactionName, isExpense ? "FOOD, TRANSPORTATION, SHOPPING, UTILITIES, OTHER" : "SALARY, FREELANCE, SUBSIDY, OTHER"
        );

        String requestBody = String.format(
                "{ \"model\": \"gpt-4o-mini\", \"messages\": [{\"role\": \"user\", \"content\": \"%s\"}], \"max_tokens\": 10 }",
                prompt
        );

        RestTemplate restTemplate = new RestTemplate();
        try {
            String response = restTemplate.postForObject(OPENAI_URL, createRequest(requestBody), String.class);
            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(response);
            return rootNode.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            return "OTHER"; // 실패하면 기본 카테고리 반환
        }
    }

    private HttpEntity<String> createRequest(String body) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + openAiApiKey);
        return new HttpEntity<>(body, headers);
    }
}
