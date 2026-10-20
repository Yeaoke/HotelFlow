package com.example.app.security;
import com.example.app.security.jwt.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration @EnableWebSecurity @RequiredArgsConstructor
public class SecurityConfig {
    private final JwtFilter filter;
    @Bean public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(csrf->csrf.disable())
            .sessionManagement(s->s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .requestCache(c->c.disable())
            .authorizeHttpRequests(a->a
                .requestMatchers(HttpMethod.POST,"/api/auth/register","/api/auth/register/verify",
                    "/api/auth/register/resend","/api/auth/login","/api/auth/refresh").permitAll()
                .requestMatchers(HttpMethod.GET,"/actuator/health","/actuator/health/liveness","/actuator/health/readiness").permitAll()
                .requestMatchers("/swagger-ui.html","/swagger-ui/**","/v3/api-docs/**").permitAll()
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .exceptionHandling(e->e
                .authenticationEntryPoint((req,res,ex)->SecurityErrors.write(res,401,"Authentication required",req.getRequestURI()))
                .accessDeniedHandler((req,res,ex)->SecurityErrors.write(res,403,"Access denied",req.getRequestURI())))
            .logout(l->l.disable())
            .addFilterBefore(filter,UsernamePasswordAuthenticationFilter.class).build();
    }
    @Bean public FilterRegistrationBean<JwtFilter> jwtFilterRegistration() {
        var registration=new FilterRegistrationBean<>(filter); registration.setEnabled(false); return registration;
    }
    @Bean public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
    @Bean public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
