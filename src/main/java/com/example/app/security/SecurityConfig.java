package com.example.app.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.example.app.security.handlers.CustomAccessDeniedHandler;
import com.example.app.security.handlers.CustomLogoutHandler;
import com.example.app.services.UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtService jwtService;

    private final UserService userService;

    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    
    private final CustomLogoutHandler customLogouthandler;

    public SecurityConfig(
        JwtService jwtService,
        UserService userService,
        CustomLogoutHandler logoutHandler,
        CustomAccessDeniedHandler accessDeniedHandler
    ) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.customAccessDeniedHandler = accessDeniedHandler;
        this.customLogouthandler = logoutHandler;
    }

    /*
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        
        http.csrf(AbstractHttpConfigurer::disable);

        http.authorizeHttpRequests(
            auth -> {
                auth.requestMatchers("/login/**","/registration/**", "/css/**", "/refresh_token/**", "/")
                    .permitAll();
                auth.requestMatchers("/admin/**").hasAuthority("ADMIN");
                auth.anyRequest().authenticated();
            });
            .userService.
    
        return http.build();
    }
    */

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration cfg) {
        return cfg.getAuthenticationManager();
    } 
}
