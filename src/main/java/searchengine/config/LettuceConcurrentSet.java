package searchengine.config;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;

import java.util.Set;

public class LettuceConcurrentSet {
    private final RedisCommands<String, String> commands;
    private String key;

    public LettuceConcurrentSet(String key) {
        RedisClient redisClient = RedisClient.create("redis://" + "localhost" + ":" + 6379);
        commands = redisClient.connect().sync();
        this.key = key;
        commands.del(key);
    }

    public boolean add(String value) {
        return commands.sadd(key, value) == 1;
    }

    public boolean remove(String value) {
        return commands.srem(key, value) == 1;
    }

    public boolean contains(String value) {
        return commands.sismember(key, value);
    }

    public Set<String> getAll() {
        return commands.smembers(key);
    }

    public void close() {
        commands.getStatefulConnection().close();
    }

//    public static void main(String[] args) {
//        LettuceConcurrentSet redisSet = new LettuceConcurrentSet(
//                "localhost", 6379, "mySet");
//        redisSet.add("value1");
//        redisSet.add("value2");
//
//        System.out.println("All elements: " + redisSet.getAll());
//        System.out.println("Contains value1: " + redisSet.contains("value1"));
//        System.out.println("Contains value3: " + redisSet.contains("value3"));
//
//        redisSet.close();
//    }

}