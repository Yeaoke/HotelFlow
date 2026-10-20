package com.example.app.repos.token;
import com.example.app.models.token.RefreshToken;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.util.*;
public interface TokenRepository extends JpaRepository<RefreshToken,UUID> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from RefreshToken t join fetch t.user where t.id = :id")
    Optional<RefreshToken> findByIdForUpdate(@Param("id") UUID id);
}
