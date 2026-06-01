package com.appointment.system.repository;

import com.appointment.system.entity.User;
import com.appointment.system.enums.AccountStatus;
import com.appointment.system.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<User> findByPersonId(Long personId);

    List<User> findByRole(Role role);

    List<User> findByRoleAndAccountStatus(Role role, AccountStatus accountStatus);

    @Query("SELECT u FROM User u WHERE u.person.isDeleted = true AND u.createdAt < :cutoff")
    List<User> findByPersonIsDeletedTrueAndCreatedAtBefore(@Param("cutoff") LocalDateTime cutoff);
}
