package com.jamify.auth.repository;

import com.jamify.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    // Find user by their Spotify ID
    Optional<User> findBySpotifyId(String spotifyId);

    // Find all active hosts whose token expires soon
    @Query("""
        SELECT u FROM User u
        WHERE u.isActiveHost = true
        AND u.tokenExpiresAt < :expiryThreshold
        AND u.deletedAt IS NULL
    """)
    List<User> findActiveHostsWithExpiringTokens(OffsetDateTime expiryThreshold);

    // Find user by ID only if not soft deleted
    @Query("""
        SELECT u FROM User u
        WHERE u.id = :id
        AND u.deletedAt IS NULL
    """)
    Optional<User> findActiveById(UUID id);
}