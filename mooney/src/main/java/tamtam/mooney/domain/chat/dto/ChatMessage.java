package tamtam.mooney.domain.chat.dto;

import java.time.LocalDateTime;

public record ChatMessage(
        String id,          // UUID (String 타입으로 저장)
        String userId,      // 유저 식별자
        String role,        // USER 또는 GPT로 역할 구분
        String message,     // 메시지 내용
        LocalDateTime timestamp
) {}