package ws;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);

//        ApplicationContext context = new AnnotationConfigApplicationContext(StorageConfig.class);
//        StorageInterface storage = context.getBean(StorageInterface.class);

    }
}
