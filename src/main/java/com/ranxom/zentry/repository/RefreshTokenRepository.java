package com.ranxom.zentry.repository;

import com.ranxom.zentry.model.RefreshToken;
import com.ranxom.zentry.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    void deleteByUser(User user); // For rotation: one session per user

}
