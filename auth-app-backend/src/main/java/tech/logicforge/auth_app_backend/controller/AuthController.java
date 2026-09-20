package tech.logicforge.auth_app_backend.controller;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import tech.logicforge.auth_app_backend.config.SecurityConfig.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import tech.logicforge.auth_app_backend.dtos.LoginRequest;
import tech.logicforge.auth_app_backend.dtos.TokenResponse;
import tech.logicforge.auth_app_backend.dtos.UserDto;
import tech.logicforge.auth_app_backend.entity.User;
import tech.logicforge.auth_app_backend.repository.UserRepository;
import tech.logicforge.auth_app_backend.security.JwtService;
import tech.logicforge.auth_app_backend.service.AuthService;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
public class AuthController {

    private final AuthService authService;
//    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final ModelMapper mapper;
//    private final CookieService cookieService;


    @PostMapping("/register")
    public ResponseEntity<UserDto> registerUser(@RequestBody UserDto userDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.registerUser(userDto));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest loginRequest, HttpServletResponse response) {

        Authentication authenticate = authenticate(loginRequest);
        User user = (User) authenticate.getPrincipal();

        if (!user.isEnable()) {
            throw new DisabledException("User account is currently disabled");
        }

//        String jti = UUID.randomUUID().toString();
//        var refreshTokenOb = RefreshToken.builder()
//                .jti(jti)
//                .user(user)
//                .createdAt(Instant.now())
//                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
//                .revoked(false)
//                .build();

        // 3. Persistent session storage tracking
//        refreshTokenRepository.save(refreshTokenOb);

        // 4. Token Generation utilizing explicit JTI tracking markers
        String accessToken = jwtService.generateAccessToken(user);
//        String refreshToken = jwtService.generateRefreshToken(user, refreshTokenOb.getJti());

        // 5. Secure cookie mapping and security policy execution
//        cookieService.attachRefreshCookie(response, refreshToken, (int) jwtService.getRefreshTtlSeconds());
//        cookieService.addNoStoreHeaders(response);

        TokenResponse tokenResponse = TokenResponse.of(accessToken, "", jwtService.getAccessTtlSeconds(), mapper.map(user, UserDto.class));
        return ResponseEntity.ok(tokenResponse);
    }

    private Authentication authenticate(LoginRequest loginRequest) {
        try {
            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password())
            );
        } catch (Exception e) {
            throw new BadCredentialsException("Invalid Username or Password !!");
        }
    }

//    @PostMapping("/refresh")
//    public ResponseEntity<TokenResponse> refreshToken(
//            @RequestBody(required = false) RefreshTokenRequest body,
//            HttpServletResponse response,
//            HttpServletRequest request
//    ) {
//        String refreshToken = readRefreshTokenFromRequest(body, request)
//                .orElseThrow(() -> new BadCredentialsException("Refresh token is missing"));
//
//        if (!jwtService.isRefreshToken(refreshToken)) {
//            throw new BadCredentialsException("Invalid Refresh Token Type");
//        }
//
//        String jti = jwtService.getJti(refreshToken);
//        UUID userId = jwtService.getUserId(refreshToken);
//
//        RefreshToken storedRefreshToken = refreshTokenRepository.findByJti(jti)
//                .orElseThrow(() -> new BadCredentialsException("Refresh token not recognized"));
//
//        if (storedRefreshToken.isRevoked()) {
//            throw new BadCredentialsException("Refresh token expired or revoked");
//        }
//
//        if (storedRefreshToken.getExpiresAt().isBefore(Instant.now())) {
//            throw new BadCredentialsException("Refresh token expired");
//        }
//
//        if (!storedRefreshToken.getUser().getId().equals(userId)) {
//            throw new BadCredentialsException("Refresh token does not belong to this user");
//        }
//
//        // Execute Refresh Token Rotation strategy to minimize reuse hijacking risks
//        storedRefreshToken.setRevoked(true);
//        String newJti = UUID.randomUUID().toString();
//        storedRefreshToken.setReplacedByToken(newJti);
//        refreshTokenRepository.save(storedRefreshToken);
//
//        User user = storedRefreshToken.getUser();
//
//        var newRefreshTokenOb = RefreshToken.builder()
//                .jti(newJti)
//                .user(user)
//                .createdAt(Instant.now())
//                .expiresAt(Instant.now().plusSeconds(jwtService.getRefreshTtlSeconds()))
//                .revoked(false)
//                .build();
//
//        refreshTokenRepository.save(newRefreshTokenOb);
//
//        String newAccessToken = jwtService.generateAccessToken(user);
//        String newRefreshToken = jwtService.generateRefreshToken(user, newRefreshTokenOb.getJti());
//
//        cookieService.attachRefreshCookie(response, newRefreshToken, (int) jwtService.getRefreshTtlSeconds());
//        cookieService.addNoStoreHeaders(response);
//
//        return ResponseEntity.ok(TokenResponse.of(newAccessToken, newRefreshToken, jwtService.getAccessTtlSeconds(), mapper.map(user, UserDto.class)));
//    }
//
//    @PostMapping("/logout")
//    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
//        readRefreshTokenFromRequest(null, request).ifPresent(token -> {
//            try {
//                if (jwtService.isRefreshToken(token)) {
//                    String jti = jwtService.getJti(token);
//                    refreshTokenRepository.findByJti(jti).ifPresent(rt -> {
//                        rt.setRevoked(true);
//                        refreshTokenRepository.save(rt);
//                    });
//                }
//            } catch (JwtException ignored) {
//                // Log context if necessary
//            }
//        });
//
//        cookieService.clearRefreshCookie(response);
//        cookieService.addNoStoreHeaders(response);
//        SecurityContextHolder.clearContext();
//        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
//    }
}
