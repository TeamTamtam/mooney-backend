package tamtam.mooney.global.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

@RequiredArgsConstructor
public class GenericRedisRepository<T> implements RedisRepository<T> {

    private final RedisTemplate<String, T> redisTemplate;

    @Override
    public void save(String key, T data) {
        redisTemplate.opsForList().rightPush(key, data);
    }

    @Override
    public List<T> findAll(String key) {
        return redisTemplate.opsForList().range(key, 0, -1);
    }
}
