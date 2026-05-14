package com.appointment.system.repository;

import com.appointment.system.entity.Provider;
import com.appointment.system.entity.TimeBlock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TimeBlockRepository extends JpaRepository<TimeBlock, Long> {

    List<TimeBlock> findByProvider(Provider provider);

    List<TimeBlock> findByProviderAndStartDateTimeBetween(Provider provider, LocalDateTime from, LocalDateTime to);

    @Query("SELECT tb FROM TimeBlock tb WHERE tb.provider = :provider AND tb.startDateTime < :dayEnd AND tb" +
            ".endDateTime > :dayStart")
    List<TimeBlock> findByProviderOverlappingDay(@Param("provider") Provider provider,
                                                 @Param("dayStart") LocalDateTime dayStart,
                                                 @Param("dayEnd") LocalDateTime dayEnd);
}
