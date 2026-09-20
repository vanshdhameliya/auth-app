package tech.logicforge.auth_app_backend.config;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tech.logicforge.auth_app_backend.repository.UserRepository;
import tech.logicforge.auth_app_backend.security.JwtService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final Logger logger =
            LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String header = request.getHeader("Authorization");
        logger.info("Authorization header: {}", header);

              if (header != null && header.startsWith("Bearer ") &&
                      SecurityContextHolder.getContext().getAuthentication() == null) {

                  String token = header.substring(7);

            try {

                String userId = jwtService.extractSubject(token);
                UUID userUuid = UUID.fromString(userId);

                if (jwtService.isTokenValid(token, userUuid)) {

                    userRepository.findById(userUuid).ifPresent(user -> {

                        if (user.isEnable()) {

                            List<GrantedAuthority> authorities =
                                    user.getRoles() == null ?
                                            List.of() :
                                            user.getRoles().stream()
                                            .map(role ->
                                                    new SimpleGrantedAuthority(role.getName()))
                                                    .collect(Collectors.toList());

                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(user, null, authorities);

                         //  extra request metadata, like the user's IP address and browser session details, to the authentication object.
                            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                            // Inject the user session directly into the active Security Context
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        } else {
                            logger.warn("User with ID {} is disabled", userUuid);
                        }
                    });
                }
            } catch (ExpiredJwtException e) {
                logger.warn("JWT token has expired");
                request.setAttribute("error", "Token Expired");
            } catch (Exception e) {
                logger.error("JWT token verification failed: {}", e.getMessage());
                request.setAttribute("error", "Invalid Token");
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {

        return request.getRequestURI().startsWith("/api/v1/auth");
    }
}
