package org.example.be.service;

import org.example.be.DTO.TestDTO;
import org.example.be.cfg.kafka.KafkaProducerService;
import org.example.be.model.TestEntity;
import org.example.be.repository.TestRepository;
import org.example.be.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@EnableCaching
public class TestService {
    @Autowired
    TestRepository testRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private KafkaProducerService kafkaProducerService;

    public TestDTO generate() {
        TestDTO testDTO = new TestDTO();

        String[] taskAndAnswer = generateMathTask();
        String task = taskAndAnswer[0];

        testDTO.setTask(task);
        testDTO.setCorrectAnswer(taskAndAnswer[1]);
        return testDTO;
    }


    public TestDTO submitAnswer(TestDTO testDTO) {
        String task = testDTO.getTask();
        String correctAnswer = testDTO.getCorrectAnswer();

        Optional<TestEntity> existingTestEntity = Optional.ofNullable(testRepository.findByUsername(testDTO.getUsername()));

        TestEntity testEntity;

        if (existingTestEntity.isPresent()) {
            testEntity = existingTestEntity.get();
            testEntity.setUserAnswer(testDTO.getUserAnswer());
            testEntity.setTask(task);
            testEntity.setCorrectAnswer(correctAnswer);

            if (testDTO.getUserAnswer().equals(correctAnswer)) {
                testEntity.setScore(testEntity.getScore() + 1);
            }

        } else {
            testEntity = new TestEntity();
            testEntity.setUsername(testDTO.getUsername());
            testEntity.setTask(task);
            testEntity.setUserAnswer(testDTO.getUserAnswer());
            testEntity.setCorrectAnswer(correctAnswer);
            testEntity.setDate(new Date(System.currentTimeMillis()));

            if (testDTO.getUserAnswer().equals(correctAnswer)) {
                testEntity.setScore(1);
            } else {
                testEntity.setScore(0);
            }
        }

        testRepository.save(testEntity);

        String currentTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String message = "User: " + testDTO.getUsername() + ", Task: " + task + ", User Answer: " + testDTO.getUserAnswer() + ", Date and Time: " + currentTime;
        kafkaProducerService.sendMessage("test-topic", message);

        testDTO.setScore(testEntity.getScore());
        return testDTO;
    }

    public int getUserRank(String username) {
        List<TestEntity> allUsers = testRepository.findAll();

        List<TestEntity> sortedUsers = allUsers.stream()
                .sorted((u1, u2) -> Integer.compare(u2.getScore(), u1.getScore()))
                .toList();

        for (int i = 0; i < sortedUsers.size(); i++) {
            if (sortedUsers.get(i).getUsername().equals(username)) {
                return i + 1;
            }
        }

        return -1;
    }


    private String[] generateMathTask() {
        Random random = new Random();
        int num1 = random.nextInt(20);
        int num2 = random.nextInt(20);
        String operator = "";
        int result = 0;

        switch (random.nextInt(3)) {
            case 0:
                operator = "+";
                result = num1 + num2;
                break;
            case 1:
                operator = "-";
                result = num1 - num2;
                break;
            case 2:
                operator = "*";
                result = num1 * num2;
                break;
        }

        String task = num1 + " " + operator + " " + num2;
        String correctAnswer = String.valueOf(result);
        return new String[]{task, correctAnswer};
    }

    public void sendTaskToKafka(String task) {
        kafkaTemplate.send("test-topic", task);
    }

}
