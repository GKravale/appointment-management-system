package com.appointment.system.security;

import com.appointment.system.entity.User;
import com.appointment.system.enums.AccountStatus;
import com.appointment.system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        if (user.getAccountStatus() == AccountStatus.PENDING_VERIFICATION) {
            throw new DisabledException("Please verify your email address before logging in.");
        }
        if (user.getAccountStatus() == AccountStatus.SUSPENDED) {
            throw new LockedException("Your account has been suspended. Please contact support.");
        }
        if (user.getAccountStatus() == AccountStatus.PENDING) {
            throw new DisabledException("Your account is awaiting admin approval.");
        }

        return new CustomUserDetails(user);
    }
}