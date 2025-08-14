package io.student.pet.security;

import io.student.pet.exception.UserNotFoundException;
import io.student.pet.model.User;
import io.student.pet.repository.UserRepository;
import io.student.pet.service.JwtProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String token = parseJwt(request);

        if (token != null) {
            log.debug("JWT token найден: {}", token);
            if (jwtProvider.validateAccessToken(token)) {
                String username = jwtProvider.getUsernameFromToken(token);
                log.debug("JWT валиден. Пользователь: {}", username);

                User user = userRepository.findByUsernameWithRole(username)
                        .orElseThrow(() -> {
                            log.warn("Пользователь {} не найден в базе", username);
                            return new UserNotFoundException();
                        });

                GrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().getName());
                UsernamePasswordAuthenticationToken auth =
                        new UsernamePasswordAuthenticationToken(user, null, List.of(authority));
                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(auth);
                log.info("Аутентификация успешна для пользователя: {}", username);
            } else {
                log.warn("Невалидный JWT токен");
            }
        } else {
            log.debug("JWT токен не найден в запросе");
        }

        chain.doFilter(request, response);
    }

    private String parseJwt(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}