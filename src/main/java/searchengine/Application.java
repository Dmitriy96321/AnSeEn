package searchengine;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//docker run --name redis -p 6379:6379 -d redis
//docker run --name search-engine-db -e MYSQL_ROOT_PASSWORD=root -e MYSQL_DATABASE=search_engine -p 3306:3306 -d mysql:late
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
