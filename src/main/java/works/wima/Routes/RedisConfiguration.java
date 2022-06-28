package works.wima.Routes;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;


@Configuration
public class RedisConfiguration {


        String REDIS_HOST = System.getenv("REDIS_HOST");
        int REDIS_PORT = Integer.parseInt(System.getenv("REDIS_PORT"));
        String REDIS_PASSWORD = System.getenv("REDIS_PASSWORD");

    @Bean("redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory connectionFactory) {
        connectionFactory.getStandaloneConfiguration().setHostName(REDIS_HOST);
        connectionFactory.getStandaloneConfiguration().setPort(REDIS_PORT);
        connectionFactory.getStandaloneConfiguration().setPassword(REDIS_PASSWORD);

        RedisTemplate<String, Object> template = new RedisTemplate<>();

        template.setConnectionFactory(connectionFactory);


        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());

        return template;
    }
}