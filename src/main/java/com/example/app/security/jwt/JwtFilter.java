package com.example.app.security.jwt;
import com.example.app.services.AccessTokenService;
import com.example.app.exceptions.ApiError;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.UUID;
@Component @RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final JwtService jwt;
    private final AccessTokenService access;
    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        // Public auth endpoints authenticate their own credentials, including refresh.
        return "POST".equals(request.getMethod()) && java.util.Set.of(
            "/api/auth/login","/api/auth/register","/api/auth/register/verify",
            "/api/auth/register/resend","/api/auth/refresh").contains(request.getServletPath());
    }
    @Override protected void doFilterInternal(HttpServletRequest request,HttpServletResponse response,FilterChain chain)
            throws IOException,ServletException {
        String header=request.getHeader("Authorization");
        if (header==null) { chain.doFilter(request,response); return; }
        try {
            if (!header.regionMatches(true,0,"Bearer ",0,7)) throw new IllegalArgumentException();
            String token=header.substring(7).trim();
            var claims=jwt.parse(token,"access");
            var user=access.authenticate(token,UUID.fromString(claims.get("sid",String.class)),UUID.fromString(claims.getSubject()));
            var auth=new UsernamePasswordAuthenticationToken(user,null,user.getAuthorities());
            auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            var context=SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth); SecurityContextHolder.setContext(context);
        } catch (JwtException | IllegalArgumentException | AuthenticationException e) {
            SecurityContextHolder.clearContext();
            com.example.app.security.SecurityErrors.write(response,401,"Authentication required",request.getRequestURI());
            return;
        } catch (org.springframework.dao.DataAccessException e) {
            SecurityContextHolder.clearContext();
            com.example.app.security.SecurityErrors.write(response,503,"Authentication service unavailable",request.getRequestURI());
            return;
        }
        chain.doFilter(request,response);
    }
}
