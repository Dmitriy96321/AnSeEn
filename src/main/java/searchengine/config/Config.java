package searchengine.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.morphology.english.EnglishLuceneMorphology;
import org.apache.lucene.morphology.russian.RussianLuceneMorphology;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;

import java.io.IOException;
@Configuration
@Slf4j
public class Config {

    @Bean
    public RussianLuceneMorphology russianLuceneMorphology() throws IOException {
        return new RussianLuceneMorphology();
    }
    @Bean
    public EnglishLuceneMorphology enLuceneMorphology() throws IOException {
        return new EnglishLuceneMorphology();
    }

    @Bean
    public Jedis jedis(){
        return new Jedis("localhost",6379);
    }


}
