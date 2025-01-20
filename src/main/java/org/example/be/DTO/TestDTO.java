package org.example.be.DTO;

import lombok.Data;

import java.sql.Date;
@Data
public class TestDTO {
    Long id;
    String username;
    String task;
    String userAnswer;
    String correctAnswer;
    Date date;
    int score;
}
