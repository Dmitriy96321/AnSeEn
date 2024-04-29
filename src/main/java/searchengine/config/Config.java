package searchengine.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.LuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;

import java.io.IOException;
@Configuration
@Slf4j
public class Config {

    @Bean
    public LuceneMorphology luceneMorphology() throws IOException {
        return new RussianLuceneMorphology();
    }

    @Bean
    public Jedis jedis() throws IOException {
        return new Jedis("localhost", 6379);
    }
}
