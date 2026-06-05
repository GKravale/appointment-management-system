package com.appointment.system.service;

import com.appointment.system.entity.Provider;
import com.appointment.system.entity.TimeBlock;
import com.appointment.system.enums.BlockType;
import com.appointment.system.repository.ProviderRepository;
import com.appointment.system.repository.TimeBlockRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TimeBlockService {

    private final TimeBlockRepository timeBlockRepository;
    private final ProviderRepository providerRepository;

    public List<TimeBlock> getTimeBlocks(Long providerPersonId) {
        Provider provider =
                providerRepository.findById(providerPersonId).orElseThrow(() -> new EntityNotFoundException("Provider" +
                        " not found"));
        return timeBlockRepository.findByProvider(provider);
    }

    @Transactional
    public void create(Long providerPersonId, LocalDateTime startDateTime, LocalDateTime endDateTime, BlockType type,
                       String note) {
        if (!startDateTime.isBefore(endDateTime)) {
            throw new IllegalArgumentException("Start must be before end");
        }
        Provider provider =
                providerRepository.findById(providerPersonId).orElseThrow(() -> new EntityNotFoundException("Provider" +
                        " not found"));

        TimeBlock block = new TimeBlock();
        block.setProvider(provider);
        block.setStartDateTime(startDateTime);
        block.setEndDateTime(endDateTime);
        block.setType(type);
        block.setNote(note);
        timeBlockRepository.save(block);
    }

    @Transactional
    public void delete(Long blockId, Long providerPersonId) {
        TimeBlock block = timeBlockRepository.findById(blockId).orElseThrow(() -> new EntityNotFoundException("Time " +
                "block not found"));
        if (!block.getProvider().getId().equals(providerPersonId)) {
            throw new SecurityException("Not authorized");
        }
        timeBlockRepository.delete(block);
    }

    public List<BlockType> getAllBlockTypes() {
        return List.of(BlockType.values());
    }
}