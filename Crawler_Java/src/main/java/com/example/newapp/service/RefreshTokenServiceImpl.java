package com.example.newapp.service;

import com.example.newapp.exception.TokenRefreshException;
import com.example.newapp.model.RefreshToken;
import com.example.newapp.repo.RefreshTokenRepository;
import com.example.newapp.repo.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class RefreshTokenServiceImpl {
    @Value("${jwtRefreshExpirationMs}")
    private Long refreshTokenDurationMs;

    @Autowired
    private RefreshTokenRepository repo;

    @Autowired
    private UserRepository userRepo;

    public Optional<RefreshToken> findByToken(String token) {
        return repo.findByToken(token);
    }

    public RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(userRepo.findById(userId).get());
        refreshToken.setExpiryDate(Instant.now().plusMillis(refreshTokenDurationMs));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken = repo.save(refreshToken);
        log.info("create refresh token success : " );
        return refreshToken;
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            repo.delete(token);
            throw new TokenRefreshException(token.getToken(), "821");
        }

        return token;
    }

    @Transactional
    public int deleteByUserId(Long userId) {
        log.info("delete refresh token logout success : " );
        return repo.deleteByUser(userRepo.findById(userId).get());
    }
}
