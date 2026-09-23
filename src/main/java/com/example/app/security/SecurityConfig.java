package com.example.app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.app.security.handlers.CustomAccessDeniedHandler;
import com.example.app.security.handlers.CustomLogoutHandler;
import com.example.app.security.jwt.JwtFilter;
import com.example.app.services.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    private final CustomUserDetailsService customUserDetailsService;

    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    
    private final CustomLogoutHandler customLogouthandler;
    
    public SecurityConfig(
        CustomUserDetailsService customUserDetailsService,
        CustomLogoutHandler logoutHandler,
        CustomAccessDeniedHandler accessDeniedHandler, 
        JwtFilter jwtFilter
    ) {
        this.customUserDetailsService = customUserDetailsService;
        this.customAccessDeniedHandler = accessDeniedHandler;
        this.customLogouthandler = logoutHandler;
        this.jwtFilter = jwtFilter;
    }

    @Bean SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        
        http.csrf(AbstractHttpConfigurer::disable);

        http.authorizeHttpRequests(
            auth -> {
                auth.requestMatchers("/login/**","/registration/**", "/css/**", "/refresh_token/**", "/")
                    .permitAll();
                auth.requestMatchers("/admin/**").hasAuthority("ADMIN");
                auth.anyRequest().authenticated();
            })
            .userDetailsService(customUserDetailsService)
            .exceptionHandling(e -> {
                e.accessDeniedHandler(customAccessDeniedHandler);
                e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED));
            })
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .logout(log -> {
                log.logoutUrl("/logout")
                .addLogoutHandler(customLogouthandler)
                .logoutSuccessHandler((request, response, auth) -> SecurityContextHolder.getContext());
            });
    
        return http.build();
    }


    @Bean PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) {
        return configuration.getAuthenticationManager();
    }
}
