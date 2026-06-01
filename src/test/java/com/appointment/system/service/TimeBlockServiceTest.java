package com.appointment.system.service;

import com.appointment.system.entity.Provider;
import com.appointment.system.entity.TimeBlock;
import com.appointment.system.enums.BlockType;
import com.appointment.system.repository.ProviderRepository;
import com.appointment.system.repository.TimeBlockRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TimeBlockServiceTest {

    @Mock
    private TimeBlockRepository timeBlockRepository;
    @Mock
    private ProviderRepository providerRepository;
    @InjectMocks
    private TimeBlockService timeBlockService;

    private Provider provider() {
        Provider provider = new Provider();
        provider.setId(1L);
        return provider;
    }

    private TimeBlock timeBlock(Provider p, LocalDateTime start, LocalDateTime end) {
        TimeBlock timeBlock = new TimeBlock();
        timeBlock.setId(1L);
        timeBlock.setProvider(p);
        timeBlock.setStartDateTime(start);
        timeBlock.setEndDateTime(end);
        timeBlock.setType(BlockType.BREAK);
        return timeBlock;
    }

    @Test
    @DisplayName("Should return time blocks for existing provider")
    void getTimeBlocks_returnsBlocks() {
        Provider provider = provider();
        TimeBlock timeBlock = timeBlock(provider, LocalDateTime.of(2025, 1, 6, 12, 0),
                LocalDateTime.of(2025, 1, 6, 13, 0));

        when(providerRepository.findById(1L)).thenReturn(Optional.of(provider));
        when(timeBlockRepository.findByProvider(provider)).thenReturn(List.of(timeBlock));

        List<TimeBlock> result = timeBlockService.getTimeBlocks(1L);

        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when provider not found on getTimeBlocks")
    void getTimeBlocks_providerNotFound_throws() {
        when(providerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> timeBlockService.getTimeBlocks(99L));
    }

    @Test
    @DisplayName("Should create time block successfully")
    void create_validBlock_savesTimeBlock() {
        Provider provider = provider();
        LocalDateTime start = LocalDateTime.of(2025, 1, 6, 12, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 6, 13, 0);

        when(providerRepository.findById(1L)).thenReturn(Optional.of(provider));

        timeBlockService.create(1L, start, end, BlockType.BREAK, "Lunch");

        verify(timeBlockRepository, times(1)).save(any(TimeBlock.class));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when start is after end")
    void create_startAfterEnd_throws() {
        LocalDateTime start = LocalDateTime.of(2025, 1, 6, 14, 0);
        LocalDateTime end = LocalDateTime.of(2025, 1, 6, 12, 0);

        assertThrows(IllegalArgumentException.class, () -> timeBlockService.create(1L, start, end, BlockType.BREAK,
                null));
        verify(timeBlockRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when start equals end")
    void create_startEqualsEnd_throws() {
        LocalDateTime same = LocalDateTime.of(2025, 1, 6, 12, 0);

        assertThrows(IllegalArgumentException.class, () -> timeBlockService.create(1L, same, same, BlockType.BREAK,
                null));
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when provider not found on create")
    void create_providerNotFound_throws() {
        when(providerRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class,
                () -> timeBlockService.create(99L,
                        LocalDateTime.of(2025, 1, 6, 9, 0),
                        LocalDateTime.of(2025, 1, 6, 10, 0),
                        BlockType.BREAK, null));
    }

    @Test
    @DisplayName("Should delete time block when provider owns it")
    void delete_ownerDeletes_success() {
        Provider provider = provider();
        TimeBlock tb = timeBlock(provider,
                LocalDateTime.of(2025, 1, 6, 12, 0),
                LocalDateTime.of(2025, 1, 6, 13, 0));

        when(timeBlockRepository.findById(1L)).thenReturn(Optional.of(tb));

        timeBlockService.delete(1L, 1L);

        verify(timeBlockRepository, times(1)).delete(tb);
    }

    @Test
    @DisplayName("Should throw SecurityException when provider does not own the time block")
    void delete_notOwner_throwsSecurityException() {
        Provider provider = provider();
        TimeBlock timeBlock = timeBlock(provider, LocalDateTime.of(2025, 1, 6, 12, 0),
                LocalDateTime.of(2025, 1, 6, 13, 0));

        when(timeBlockRepository.findById(1L)).thenReturn(Optional.of(timeBlock));

        assertThrows(SecurityException.class, () -> timeBlockService.delete(1L, 99L));
        verify(timeBlockRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Should throw EntityNotFoundException when time block not found on delete")
    void delete_notFound_throws() {
        when(timeBlockRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> timeBlockService.delete(99L, 1L));
    }

    @Test
    @DisplayName("Should return all block types")
    void getAllBlockTypes_returnsAllTypes() {
        List<BlockType> types = timeBlockService.getAllBlockTypes();
        assertEquals(BlockType.values().length, types.size());
    }
}