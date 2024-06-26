package searchengine.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;
import searchengine.model.SiteEntity;


public class LettuceCache {
    private final RedisCommands<String, String> commands;
    private SiteEntity key;

    public LettuceCache(SiteEntity key) {
        RedisClient redisClient = RedisClient.create("redis://" + "localhost" + ":" + 6379);
        commands = redisClient.connect().sync();
        this.key = key;
        commands.del(key.getName());
    }

    public boolean addSet(String nameCache,String value) {
        return commands.sadd(key.getName() + "_" + nameCache, value) == 1;
    }

}