package com.jamify.auth.security;

import com.jamify.auth.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

        extractTokenFromCookie(request).ifPresent(token -> {
            if (jwtService.validateToken(token)) {
                try {
                    var userId = jwtService.extractUserId(token);
                    var auth = new UsernamePasswordAuthenticationToken(
                            userId, null, Collections.emptyList());
                    auth.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(request));
                    SecurityContextHolder.getContext()
                            .setAuthentication(auth);
                } catch (Exception e) {
                    log.warn("Could not set authentication: {}",
                            e.getMessage());
                }
            }
        });

        filterChain.doFilter(request, response);
    }

    private Optional<String> extractTokenFromCookie(
            HttpServletRequest request) {
        if (request.getCookies() == null) {
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(c -> "jamify_token".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}