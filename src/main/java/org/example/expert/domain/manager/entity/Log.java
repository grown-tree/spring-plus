package org.example.expert.domain.manager.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "log")
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long todoId;
    private Long requestUserId;   // 요청한 유저
    private Long managerUserId;   // 등록하려는 담당자
    private String status;        // "SUCCESS" or "FAIL"
    private String message;       // 실패 시 에러 메시지

    @Column(nullable = false)
    private LocalDateTime createdAt;

    public Log(Long todoId, Long requestUserId, Long managerUserId, String status, String message) {
        this.todoId = todoId;
        this.requestUserId = requestUserId;
        this.managerUserId = managerUserId;
        this.status = status;
        this.message = message;
        this.createdAt = LocalDateTime.now();
    }
}
