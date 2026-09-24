package tech.logicforge.auth_app_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tech.logicforge.auth_app_backend.entity.Role;
import tech.logicforge.auth_app_backend.entity.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Service
@Getter
@Setter
public class JwtService {

    private final String secret;
    private final long accessTtlSeconds;
    private final long refreshTtlSeconds;
    private final String issuer;
    private SecretKey signingKey;

    public JwtService(
            @Value("${security.jwt.secret}") String secret,
            @Value("${security.jwt.access-ttl-seconds}") long accessTtlSeconds,
            @Value("${security.jwt.refresh-ttl-seconds}") long refreshTtlSeconds,
            @Value("${security.jwt.issuer}") String issuer) {
        this.secret = secret;
        this.accessTtlSeconds = accessTtlSeconds;
        this.refreshTtlSeconds = refreshTtlSeconds;
        this.issuer = issuer;
    }

    @PostConstruct
    protected void init() {
        if (secret == null || secret.getBytes(StandardCharsets.UTF_8).length < 64) {
            throw new IllegalArgumentException("JWT Secret key must be at least 64 bytes (512 bits) long for HS512 safety.");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        return buildToken(user, accessTtlSeconds, "access");
    }

    public String generateRefreshToken(User user, String jti) {
        return buildTokenWithJti(user, refreshTtlSeconds, "refresh", jti);
    }

    private String buildToken(User user, long ttlSeconds, String tokenType) {

        Instant now = Instant.now();
        List<String> roles = user.getRoles() == null ? List.of() :
                user.getRoles().stream().map(Role::getName).toList();

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(ttlSeconds)))
                .claims(Map.of(
                        "email", user.getEmail(),
                        "roles", roles,
                        "typ", tokenType
                ))
                .signWith(signingKey, Jwts.SIG.HS512)
                .compact();
    }

    private String buildTokenWithJti(User user, long ttlSeconds, String tokenType, String jti) {
        Instant now = Instant.now();

        return Jwts.builder()
                .id(jti)
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(ttlSeconds)))
                .claims(Map.of(
                        "email", user.getEmail(),
                        "typ", tokenType
                ))
                .signWith(signingKey, Jwts.SIG.HS512)
                .compact();
    }

    public String getJti(String token) {
        return extractClaim(token, Claims::getId);
    }

    public boolean isRefreshToken(String token) {
        String typ = extractClaim(token, claims -> claims.get("typ", String.class));
        return "refresh".equals(typ);
    }

    public UUID getUserId(String token) {
        return UUID.fromString(extractSubject(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String extractSubject(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public boolean isTokenValid(String token, UUID userId) {
        try {
            final String extractedId = extractSubject(token);
            return (extractedId.equals(userId.toString()) && !isTokenExpired(token));
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }
}
