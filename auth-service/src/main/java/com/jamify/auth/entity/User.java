package com.jamify.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Array;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "spotify_id" , unique = true , nullable = false)
    private String spotifyId;

    @Column(name = "display_name" , nullable = false)
    private String displayName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(name = "email")
    private String email;

    @Column(name = "is_premium")
    private boolean isPremium;

    @Column(name = "spotify_access_token" , nullable = false)
    private String spotifyAccessToken;

    @Column(name = "spotify_refresh_token" , nullable = false)
    private String spotifyRefreshToken;

    @Column(name = "token_expires_at" , nullable = false)
    private OffsetDateTime tokenExpiresAt;

    @Column(name = "is_active_host")
    private boolean isActiveHost;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Array(length = 10)
    @Column(name = "top_genres")
    private String[] topGenres;

    @Column(name = "created_at" , updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    int Streak;

    @PrePersist
    protected void onCreate(){
        createdAt = OffsetDateTime.now();
        updatedAt = OffsetDateTime.now();
    }

    @PreUpdate
    protected void onUpdate(){
        updatedAt = OffsetDateTime.now();
    }
}
