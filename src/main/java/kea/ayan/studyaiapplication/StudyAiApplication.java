package kea.ayan.studyaiapplication;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class StudyAiApplication {

    public static void main(String[] args) {
        SpringApplication.run(StudyAiApplication.class, args);
        System.out.println(System.getenv("OPEN_API_KEY"));
    }

}
