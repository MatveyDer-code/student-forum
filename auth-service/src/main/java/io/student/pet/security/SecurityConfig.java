package io.student.pet.security;

import io.student.pet.repository.UserRepository;
import io.student.pet.service.JwtProvider;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Profiles;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    private final JwtProvider jwtProvider;

    public SecurityConfig(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           UserRepository userRepository,
                                           org.springframework.core.env.Environment env) throws Exception {
        JwtAuthenticationFilter jwtFilter = new JwtAuthenticationFilter(jwtProvider, userRepository);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> {
                    auth.requestMatchers("/login", "/register", "/refresh").permitAll();

                    // Актуатор открыт только в dev
                    if (env.acceptsProfiles(Profiles.of("dev"))) {
                        auth.requestMatchers("/actuator/**").permitAll();
                    } else {
                        auth.requestMatchers("/actuator/**").hasRole("MODERATOR");
                    }

                    auth.requestMatchers("/test/moderator/**").hasRole("MODERATOR");
                    auth.requestMatchers("/test/teacher/**").hasRole("TEACHER");
                    auth.requestMatchers("/test/student/**").hasRole("STUDENT");
                    auth.anyRequest().authenticated();
                })
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(
                                (request, response, authException) ->
                                        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                        )
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
