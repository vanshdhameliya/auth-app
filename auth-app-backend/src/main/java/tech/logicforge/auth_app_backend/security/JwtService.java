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

    public String generateRefreshToken(User user) {
        return buildToken(user, refreshTtlSeconds, "refresh");
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

    private Claims extractAllClaims(String token) {

        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

/*    It means this method can return any object type depending on what you ask for.
      If you ask for the Expiration Date, T becomes a Date.
      If you ask for roles, T becomes a List<String>.
      function that takes a Claims object as input, and I will return something of type T.           */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {

//        parse the encrypted token string, verify its cryptographic signature
//        with your signingKey, and unlock the entire JSON payload.
        final Claims claims = extractAllClaims(token);

//        runs it against the unlocked claims object, and returns exactly what you asked for.
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
