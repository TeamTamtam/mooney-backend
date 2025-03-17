package tamtam.mooney.global.redis;

import java.util.List;

public interface RedisRepository<T> {
    void save(String key, T data);
    List<T> findAll(String key);
}