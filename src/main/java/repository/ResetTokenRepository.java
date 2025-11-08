package repository;

import model.ResetToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResetTokenRepository extends JpaRepository<ResetToken, String> {
    boolean existsByToken(String token);

    Optional<ResetToken> findByUserId(Long id);

    @Query("""
        SELECT COUNT(t) > 0 FROM ResetToken t WHERE t.token = :token AND t.expiry > CURRENT_TIMESTAMP
    """)
    boolean isTokenValid(@Param("token") String token);

    @Modifying
    @Query("""
        DELETE FROM ResetToken t WHERE t.expiry < CURRENT_TIMESTAMP
    """)
    void deleteTokensExpired();
}
