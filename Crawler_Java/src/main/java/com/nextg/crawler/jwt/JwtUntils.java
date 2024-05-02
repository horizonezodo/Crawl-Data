package com.nextg.crawler.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.nextg.crawler.service.UserDetailsImpl;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

@Component
@Slf4j
public class JwtUntils {
    @Value("${secret}")
    private String SECRET;

    @Value("${jwtExpirationMs}")
    private long jwtDurationMs;

    private Key key(){
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(SECRET));
    }

    public String getEmailFromJwtToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key()).build()
                .parseClaimsJws(token).getBody().getSubject();
    }

    public String generateTokenFromEmail(String email){
        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + jwtDurationMs))
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateJwtTokenForLogin(UserDetailsImpl userDetails){
        return generateTokenFromEmail(userDetails.getEmail());
    }

    public boolean validateJwtToken(String token){
        try{
            Jwts.parserBuilder().setSigningKey(key()).build().parse(token);
            return true;
        }catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public boolean verifyToken(String token) {
        try {
            Jws<Claims> claimsJwt = Jwts.parserBuilder().setSigningKey(key()).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

//    public boolean verifyToken2(String token){
//        try{
//            byte[] decodeSecret = Base64.getDecoder().decode(SECRET);
//            Algorithm algorithm = Algorithm.HMAC256(decodeSecret);
//            JWTVerifier verifier = JWT.require(algorithm).build();
//            verifier.verify(token);
//            return true;
//        }catch (JWTVerificationException exception){
//            return false;
//        }
//    }
}
