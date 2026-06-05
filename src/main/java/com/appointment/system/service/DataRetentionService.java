package com.appointment.system.service;

import com.appointment.system.entity.User;
import com.appointment.system.enums.AccountStatus;
import com.appointment.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DataRetentionService {

    private final UserRepository userRepository;

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void purgeDeletedAccounts() {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(30);
        List<User> toPurge = userRepository.findByPersonIsDeletedTrueAndCreatedAtBefore(cutoff);
        if (toPurge.isEmpty()) return;
        log.info("Purging {} soft-deleted accounts older than 30 days", toPurge.size());
        userRepository.deleteAll(toPurge);
    }
}