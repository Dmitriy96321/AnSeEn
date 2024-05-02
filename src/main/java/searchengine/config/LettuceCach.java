package searchengine.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import searchengine.model.LemmaEntity;
import searchengine.model.SiteEntity;
import searchengine.parser.HttpParserJsoup;

import java.util.Set;

public class LettuceCach {
    private final RedisCommands<String, String> commands;
    private SiteEntity key;

    public LettuceCach(SiteEntity key) {
        RedisClient redisClient = RedisClient.create("redis://" + "localhost" + ":" + 6379);
        commands = redisClient.connect().sync();
        this.key = key;
        commands.del(key.getName());
    }

    public boolean addSet(String value) {
        return commands.sadd(key.getName(), value) == 1;
    }

}