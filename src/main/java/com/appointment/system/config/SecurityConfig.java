package com.appointment.system.config;

import com.appointment.system.security.CustomUserDetails;
import com.appointment.system.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authenticationProvider(authenticationProvider()).authorizeHttpRequests(auth
                -> auth.requestMatchers("/", "/auth/login", "/auth/register", "/auth/forgot-password",
                "/auth/reset-password", "/auth/verify-email", "/auth/resend-verification",
                "/providers", "/providers/**", "/privacy", "/terms", "/css/**",
                "/js/**", "/uploads/**", "/webjars/**"
        ).permitAll().requestMatchers("/admin/**").hasAuthority(
                "ROLE_ADMIN").requestMatchers(
                "/provider/**").hasAuthority("ROLE_PROVIDER").requestMatchers("/client/**").hasAuthority(
                "ROLE_CLIENT").anyRequest().authenticated()).formLogin(form ->
                form.loginPage("/auth" + "/login").usernameParameter("username").passwordParameter("password")
                        .successHandler(roleBasedSuccessHandler()).failureUrl("/auth" + "/login?error=true")
                        .permitAll()).logout(logout -> logout.logoutUrl("/auth/logout")
                .logoutSuccessUrl("/auth/login?logout=true").invalidateHttpSession(true).deleteCookies("JSESSIONID")
                .permitAll()).sessionManagement(session -> session.maximumSessions(1));

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler roleBasedSuccessHandler() {
        return (request, response, authentication) -> {
            Object principal = authentication.getPrincipal();
            if (!(principal instanceof CustomUserDetails user)) {
                response.sendRedirect("/");
                return;
            }
            switch (user.getRole()) {
                case ROLE_PROVIDER -> response.sendRedirect("/provider/dashboard");
                case ROLE_CLIENT -> response.sendRedirect("/client/dashboard");
                case ROLE_ADMIN -> response.sendRedirect("/admin/dashboard");
            }
        };
    }
}