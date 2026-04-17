package com.jamify.auth.service;

import com.jamify.auth.config.SpotifyConfig;
import com.jamify.auth.dto.SpotifyTokenResponse;
import com.jamify.auth.dto.SpotifyUserProfile;
import com.jamify.auth.entity.User;
import com.jamify.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpotifyAuthService {

    private final SpotifyConfig spotifyConfig;
    private final UserRepository userRepository;
    private final RedisTemplate<String , String> redisTemplate;
    private final WebClient spotifyAuthWebClient;
    private final WebClient spotifyWebClient;

    private static final String STATE_PREFIX = "oauth:state:";
    private static final int STATE_TTL_MINUTES = 10;


    // ─── Step 1: Build the Spotify authorization URL ───────────────────

    public String buildAuthorizationUrl(String state){
        String scopes = String.join(" " , SpotifyConfig.SCOPES);

        return spotifyConfig.getAuthUrl()
                + "?client_id=" + spotifyConfig.getClientId()
                + "&response_type=code"
                + "&redirect_uri=" + spotifyConfig.getRedirectUri()
                + "&scope=" + scopes.replace(" " , "%20")
                + "&state=" + state
                + "&show_dialog=true";
    }


    // ─── Step 2: Save state to Redis ───────────────────────────────────

    public void saveState(String state){
        String key = STATE_PREFIX + state;
        redisTemplate.opsForValue().set(key , "valid" , STATE_TTL_MINUTES , TimeUnit.MINUTES);
        log.info("OAuth state saved to redis with key: {}" , key);
    }


    // ─── Step 3: Validate state from Redis ─────────────────────────────

    public boolean validateState(String state){
        String key = STATE_PREFIX + state;
        String value = redisTemplate.opsForValue().get(key);
        if(value == null){
            log.warn("OAuth state validation failed - state not found: {}" , state);
            return false;
        }
        // Delete after validation — single use
        redisTemplate.delete(key);
        return true;
    }


    // ─── Step 4: Exchange code for tokens ──────────────────────────────

    public SpotifyTokenResponse exchangeCodeForTokens(String code){
        MultiValueMap<String , String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type" , "authorization_code");
        formData.add("code" , code);
        formData.add("redirect_uri" , spotifyConfig.getRedirectUri());

        String credentials = spotifyConfig.getClientId()
                + ":" + spotifyConfig.getClientSecret();
        String encodedCredentials = java.util.Base64.getEncoder()
                .encodeToString(credentials.getBytes());

        SpotifyTokenResponse response = spotifyAuthWebClient.post()
                .uri("/api/token")
                .header("Authorization" , "Basic " + encodedCredentials)
                .header("Content-Type",
                        "application/x-www-form-urlencoded")
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(SpotifyTokenResponse.class)
                .block();

        log.info("Successfully exchanged authorization code for tokens");
        return response;
    }


    // ─── Step 5: Fetch user profile from Spotify ───────────────────────

    public SpotifyUserProfile fetchUserProfile(String accessToken) {
        log.info("Fetching profile with token length: {}",
                accessToken.length());

        return spotifyWebClient.get()
                .uri("/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(),
                        clientResponse -> clientResponse.bodyToMono(String.class)
                                .map(body -> {
                                    log.error("Spotify /me error body: {}", body);
                                    return new RuntimeException(body);
                                }))
                .bodyToMono(SpotifyUserProfile.class)
                .block();
    }


    // ─── Step 6: Save or update user in database ───────────────────────

    public User saveOrUpdateUser(SpotifyUserProfile profile , SpotifyTokenResponse tokens){

        Optional<User> existingUser = userRepository.findBySpotifyId(profile.getId());

        OffsetDateTime tokenExpiresAt = OffsetDateTime.now().plusSeconds(tokens.getExpiresIn());

        if(existingUser.isPresent()){
            // Update existing user
            User user = existingUser.get();
            user.setDisplayName(profile.getDisplayName());
            user.setAvatarUrl(profile.getAvatarUrl());
            user.setEmail(profile.getEmail());
            user.setPremium(profile.isPremium());
            user.setSpotifyAccessToken(tokens.getAccessToken());
            user.setSpotifyRefreshToken(tokens.getRefreshToken());
            user.setTokenExpiresAt(tokenExpiresAt);
            log.info("Updated existing user: {}", profile.getId());
            return userRepository.save(user);
        }
        else {
            // Create new user
            User newUser = User.builder()
                    .spotifyId(profile.getId())
                    .displayName(profile.getDisplayName())
                    .avatarUrl(profile.getAvatarUrl())
                    .email(profile.getEmail())
                    .isPremium(profile.isPremium())
                    .spotifyAccessToken(tokens.getAccessToken())
                    .spotifyRefreshToken(tokens.getRefreshToken())
                    .tokenExpiresAt(tokenExpiresAt)
                    .topGenres(new String[]{})
                    .build();
            log.info("Created new user: {}", profile.getId());
            return userRepository.save(newUser);
        }
    }


    // ─── Step 7: Cache access token in Redis ───────────────────────────

    public void cacheAccessToken(UUID userId , String accessToken , int expiresInSeconds){

        String key = "token:" + userId;
        redisTemplate.opsForValue().set(
                key,
                accessToken,
                expiresInSeconds,
                TimeUnit.SECONDS
        );
        log.info("Cached access token for user: {}", userId);
    }
}
