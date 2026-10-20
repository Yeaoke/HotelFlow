package com.example.app.services;
import com.example.app.exceptions.RateLimitException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.nio.charset.StandardCharsets;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
@Service
public class OtpService {

    private final StringRedisTemplate redis;
    
    private final VerificationMailer mailer;

    private final byte[] pepper;

    private final SecureRandom random=new SecureRandom();

    private static final DefaultRedisScript<Long> ISSUE = new DefaultRedisScript<>(
        """
            if redis.call('EXISTS',KEYS[2]) == 1 then return 0 end
            if tonumber(redis.call('GET',KEYS[3]) or '0') >= 5 then return 0 end
            if tonumber(redis.call('GET',KEYS[4]) or '0') >= 20 then return 0 end
            redis.call('SET',KEYS[2],'1','EX',60)
            for i=3,4 do
                if redis.call('INCR',KEYS[i]) == 1 then redis.call('EXPIRE',KEYS[i],3600) end
            end
            redis.call('HSET',KEYS[1],'digest',ARGV[1],'attempts',0)
            redis.call('EXPIRE',KEYS[1],300)
            return 1
        """,
        Long.class
    );
    
    private static final DefaultRedisScript<Long> VERIFY=new DefaultRedisScript<>(
        """
            local digest=redis.call('HGET',KEYS[1],'digest')
            if not digest then return 0 end
            local attempts=redis.call('HINCRBY',KEYS[1],'attempts',1)
            if digest == ARGV[1] then redis.call('DEL',KEYS[1]); return 1 end
            if attempts >= 5 then redis.call('DEL',KEYS[1]) end
            return 0
        """,
        Long.class
    );

    public OtpService(
        StringRedisTemplate redis,
        VerificationMailer mailer,
        @Value("${security.otp.secret}") String secret
    ) {
        if (secret.length() < 32) {
            throw new IllegalArgumentException("OTP secret must be at least 32 characters");
        }

        this.redis = redis;
        this.mailer = mailer;
        this.pepper = secret.getBytes(StandardCharsets.UTF_8);
    }

    public void issue(
        UUID userId,String email,String ip
    ) {
        String code = String.format(Locale.ROOT,"%06d",random.nextInt(1_000_000));

        String key = "otp:" + userId;

        Long allowed=redis.execute(ISSUE,List.of(key, "otp:cooldown:" + userId, "otp:email:"  +digest(email.toLowerCase(Locale.ROOT)), "otp:ip:" + digest(ip)), digest(userId + ":" + code));
        
        if (!Long.valueOf(1).equals(allowed)) {
            throw new RateLimitException();
        }
        
        try {
            mailer.send(email,code);
        } catch (RuntimeException e) {
            redis.delete(key);
            throw e;
        }
    }

    public boolean verify(
        UUID userId,
        String code
    ) {
        if (code == null || !code.matches("[0-9]{6}")) {
            return false;
        }

        return Long.valueOf(1).equals(redis.execute(VERIFY, List.of("otp:" + userId), digest(userId + ":" + code)));
    }
    private String digest(
        String value
    ) {
        try {

            Mac mac = Mac.getInstance("HmacSHA256");

            mac.init(new SecretKeySpec(pepper, "HmacSHA256"));

            return HexFormat.of().formatHex(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (java.security.GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }
}
