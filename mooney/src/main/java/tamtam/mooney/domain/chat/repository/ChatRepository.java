package tamtam.mooney.domain.chat.repository;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
public class ChatRepository {
    private final RedisTemplate<String, Object> redisTemplate;
    private static final long CHAT_TTL = 86400; // 24시간

    public ChatRepository(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void saveChat(String userId, String chatHistory) {
        redisTemplate.opsForValue().set("chat:" + userId, chatHistory, CHAT_TTL, TimeUnit.SECONDS);
    }

    public String getChatHistory(String userId) {
        return (String) redisTemplate.opsForValue().get("chat:" + userId);
    }
}
