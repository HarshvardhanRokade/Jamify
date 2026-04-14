package com.jamify.auth.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class SpotifyConfig {

    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    @Value("${spotify.redirect-uri}")
    private String redirectUri;

    @Value("${spotify.auth-url}")
    private String authUrl;

    @Value("${spotify.token-url}")
    private String tokenUrl;

    @Value("${spotify.api-url}")
    private String apiUrl;

    public static final String[] SCOPES = {
            "user-read-currently-playing",
            "user-read-playback-state",
            "user-read-private",
            "user-read-email",
            "user-follow-read",
            "user-top-read",
            "user-read-recently-played",
            "playlist-modify-public"
    };

}
