package com.example.app.security;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import com.fasterxml.jackson.databind.ObjectMapper;
public final class SecurityErrors {
    
    private static final ObjectMapper JSON=new ObjectMapper();
    
    private SecurityErrors() {}
    
    public static void write(
        HttpServletResponse response,
        int status,String message,
        String path
    ) throws IOException {

        response.setStatus(status); response.setContentType("application/json");

        response.setCharacterEncoding("UTF-8");

        JSON.writeValue(response.getWriter(),java.util.Map.of("timestamp",Instant.now().toString(), "status",status,"error",org.springframework.http.HttpStatus.valueOf(status).getReasonPhrase(), "message",message,"path",path));
    }
}
