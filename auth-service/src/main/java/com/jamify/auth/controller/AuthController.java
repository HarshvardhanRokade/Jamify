package com.jamify.auth.controller;

import com.jamify.auth.entity.User;
import com.jamify.auth.service.JwtService;
import com.jamify.auth.service.SpotifyAuthService;
import com.jamify.auth.dto.SpotifyTokenResponse;
import com.jamify.auth.dto.SpotifyUserProfile;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final SpotifyAuthService spotifyAuthService;
    private final JwtService jwtService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    // ─── Login endpoint ─────────────────────────────────────────────────
    // Frontend calls this to start the OAuth flow

    @GetMapping("/login")
    public void login(HttpServletResponse response) throws IOException {
        // Generate random state for CSRF protection
        String state = UUID.randomUUID().toString();

        // Save state to Redis
        spotifyAuthService.saveState(state);

        // Build Spotify auth URL
        String authUrl = spotifyAuthService.buildAuthorizationUrl(state);

        log.info("Redirecting user to Spotify authorization");

        // Redirect user to Spotify
        response.sendRedirect(authUrl);
    }

    // ─── Callback endpoint ──────────────────────────────────────────────
    // Spotify redirects here after user approves

    @GetMapping("/callback")
    public void callback(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            @RequestParam(value = "error", required = false) String error,
            HttpServletResponse response) throws IOException {

        // Handle user denial
        if (error != null) {
            log.warn("User denied Spotify authorization: {}", error);
            response.sendRedirect(frontendUrl + "?error=access_denied");
            return;
        }

        // Validate state - CSRF protection
        if (!spotifyAuthService.validateState(state)) {
            log.warn("Invalid OAuth state received");
            response.sendRedirect(frontendUrl + "?error=invalid_state");
            return;
        }

        try {
            // Exchange code for tokens
            SpotifyTokenResponse tokens =
                    spotifyAuthService.exchangeCodeForTokens(code);

            // Fetch user profile from Spotify
            SpotifyUserProfile profile =
                    spotifyAuthService.fetchUserProfile(
                            tokens.getAccessToken());

            // Save or update user in database
            User user = spotifyAuthService.saveOrUpdateUser(
                    profile, tokens);

            // Cache access token in Redis
            spotifyAuthService.cacheAccessToken(
                    user.getId(),
                    tokens.getAccessToken(),
                    tokens.getExpiresIn()
            );

            // Generate our own JWT
            String jwt = jwtService.generateToken(
                    user.getId(), user.getSpotifyId());

            // Set JWT as httpOnly cookie
            ResponseCookie cookie = ResponseCookie
                    .from("jamify_token", jwt)
                    .httpOnly(true)
                    .secure(false) // true in production
                    .sameSite("Lax") // Lax for local dev, Strict in production
                    .maxAge(Duration.ofDays(7))
                    .path("/")
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE,
                    cookie.toString());

            log.info("User authenticated successfully: {}",
                    user.getSpotifyId());

            // Redirect to frontend
            response.sendRedirect(frontendUrl + "/dashboard");

        } catch (Exception e) {
            log.error("Authentication failed: {}", e.getMessage());
            response.sendRedirect(frontendUrl + "?error=auth_failed");
        }
    }

    // ─── Logout endpoint ────────────────────────────────────────────────

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @CookieValue(value = "jamify_token",
                    required = false) String token,
            HttpServletResponse response) {

        if (token != null) {
            jwtService.blacklistSession(token);
        }

        // Clear the cookie
        ResponseCookie cookie = ResponseCookie
                .from("jamify_token", "")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .maxAge(Duration.ZERO)
                .path("/")
                .build();

        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());

        return ResponseEntity.ok(
                Map.of("message", "Logged out successfully"));
    }

    // ─── Health check for auth service ──────────────────────────────────

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "ok"));
    }
}