package tech.logicforge.auth_app_backend.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;


@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

    http
         .csrf(AbstractHttpConfigurer::disable)
         .cors(Customizer.withDefaults())
         .sessionManagement(sm -> sm.
                 sessionCreationPolicy(SessionCreationPolicy.STATELESS))
         .authorizeHttpRequests(authorizeHttpRequests ->
                 authorizeHttpRequests.requestMatchers("/api/v1/auth/register").permitAll()
                                .anyRequest().authenticated())

            .exceptionHandling(ex ->
                    ex.authenticationEntryPoint(
                            (request, response, authException) -> {
                                authException.printStackTrace();

                                response.setStatus(401);
                                response.setContentType("application/json");
                                String msg = "Unauthorized access! "+ authException.getMessage();

                                Map<String,String> errorMap =
                                        Map.of("message",msg,"statusCode",new Integer(401).toString());

                                var objectMapper = new ObjectMapper();
                                response.getWriter().write(objectMapper.writeValueAsString(errorMap));

                            }
                            ))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .httpBasic(Customizer.withDefaults());

    return http.build();
}


// In-Memory Data
//@Bean
public UserDetailsService users() {

    User.UserBuilder userBuilder = User.withDefaultPasswordEncoder();
    UserDetails user1 = userBuilder.username("vansh").password("vansh123").roles("ADMIN").build();
    UserDetails user2 = userBuilder.username("shiva").password("xyz").roles("ADMIN").build();
    UserDetails user3 = userBuilder.username("durgesh").password("").roles("USER").build();

    return new InMemoryUserDetailsManager(user1, user2, user3);
}

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
