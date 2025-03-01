package tamtam.mooney.global.security;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Service
@Transactional
@RequiredArgsConstructor
public class RedisService {
    private final RedisTemplate<String,Object> redisTemplate;

    // Redis에 값을 설정하고, 지정된 시간 후에 만료되도록 설정
    public void setValuesWithTimeout(String key, String value, Duration timeout){
        redisTemplate.opsForValue().set(key,value,timeout);
    }

    // 키가 존재하지 않을 경우만 값을 설정
    public boolean setValuesWithTimeoutIfAbsent(String key, String value, Duration timeout){
        return redisTemplate.opsForValue().setIfAbsent(key, value, timeout);
    }

    @Transactional(readOnly = true)
    public Object getValues(String key){
        return redisTemplate.opsForValue().get(key);
    }

    // email로 리프레시 토큰을 삭제
    public void deleteValues(String key) {
        redisTemplate.delete(key);
    }
}
