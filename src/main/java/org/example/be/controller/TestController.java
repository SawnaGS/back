package org.example.be.controller;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.example.be.DTO.TestDTO;
import org.example.be.cfg.kafka.KafkaProducerService;
import org.example.be.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
@CrossOrigin("http://localhost:4200")
public class TestController {

    @Autowired
    private TestService testService;
    @Autowired
    private KafkaProducerService kafkaProducerService;

    @GetMapping("/generate")
    public ResponseEntity<TestDTO> generateTask() {
        return new ResponseEntity<>(testService.generate(), HttpStatus.OK);
    }

    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitAnswer(@RequestBody TestDTO testDTO) {
        TestDTO submittedTask = testService.submitAnswer(testDTO);
        int rank = testService.getUserRank(testDTO.getUsername());
        Map<String, Object> response = new HashMap<>();
        response.put("submittedTask", submittedTask);
        response.put("rank", rank);
        testService.sendTaskToKafka(submittedTask.getTask());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}

