package org.example.expert.domain.manager.service;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.manager.entity.Log;
import org.example.expert.domain.manager.repository.LogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogService {

    private final LogRepository logRepository;

    // REQUIRES_NEW = 기존 트랜잭션과 별개로 새 트랜잭션 생성(실패시에도 로그 저장)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveLog(Long todoId, Long requestUserId, Long managerUserId, String status, String message) {
        logRepository.save(new Log(todoId, requestUserId, managerUserId, status, message));
    }
}
