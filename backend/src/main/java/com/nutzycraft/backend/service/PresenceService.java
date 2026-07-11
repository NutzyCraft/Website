package com.nutzycraft.backend.service;

import com.nutzycraft.backend.entity.UserPresence;
import com.nutzycraft.backend.repository.PresenceRepository;
import com.nutzycraft.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Tracks live WebSocket sessions per user and maintains their online/last-seen
 * presence in MongoDB. Driven by STOMP connect/disconnect events, so each
 * session produces at most two writes (online on first connect, offline on last
 * disconnect) — no periodic heartbeat writes.
 */
@Service
public class PresenceService {

    // Grace period before flipping a user offline. Absorbs the brief gap while a
    // user navigates between pages (old socket closes, new one opens).
    private static final long OFFLINE_GRACE_SECONDS = 8;

    private final PresenceRepository presenceRepository;
    private final UserRepository userRepository;

    // email -> set of that user's live WebSocket session ids
    private final Map<String, Set<String>> sessionsByUser = new ConcurrentHashMap<>();
    // email -> pending "mark offline" task, cancelled if the user reconnects in time
    private final Map<String, ScheduledFuture<?>> pendingOffline = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "presence-offline-scheduler");
        t.setDaemon(true);
        return t;
    });

    public PresenceService(PresenceRepository presenceRepository, UserRepository userRepository) {
        this.presenceRepository = presenceRepository;
        this.userRepository = userRepository;
    }

    public synchronized void onConnect(String email, String sessionId) {
        if (email == null || sessionId == null) return;

        // A reconnect cancels any pending offline flip.
        ScheduledFuture<?> pending = pendingOffline.remove(email);
        if (pending != null) pending.cancel(false);

        Set<String> sessions = sessionsByUser.computeIfAbsent(email, k -> ConcurrentHashMap.newKeySet());
        boolean wasEmpty = sessions.isEmpty();
        sessions.add(sessionId);
        if (wasEmpty) {
            writePresence(email, true);
        }
    }

    public synchronized void onDisconnect(String email, String sessionId) {
        if (email == null || sessionId == null) return;

        Set<String> sessions = sessionsByUser.get(email);
        if (sessions == null) return;
        sessions.remove(sessionId);
        if (sessions.isEmpty()) {
            // Defer the offline flip briefly; a page navigation reconnects almost immediately.
            ScheduledFuture<?> task = scheduler.schedule(
                    () -> flipOfflineIfStillGone(email), OFFLINE_GRACE_SECONDS, TimeUnit.SECONDS);
            pendingOffline.put(email, task);
        }
    }

    private synchronized void flipOfflineIfStillGone(String email) {
        pendingOffline.remove(email);
        Set<String> sessions = sessionsByUser.get(email);
        if (sessions == null || sessions.isEmpty()) {
            sessionsByUser.remove(email);
            writePresence(email, false);
        }
    }

    private void writePresence(String email, boolean online) {
        try {
            userRepository.findByEmail(email).ifPresent(user -> {
                UserPresence presence = new UserPresence();
                presence.setUserId(user.getId());
                presence.setOnline(online);
                presence.setLastSeen(LocalDateTime.now());
                presenceRepository.save(presence); // upsert keyed by userId (_id)
            });
        } catch (Exception e) {
            // Presence is soft state — never let a write failure disrupt the socket lifecycle.
            System.err.println("PresenceService: failed to write presence for " + email + ": " + e.getMessage());
        }
    }

    /**
     * On startup no client is connected yet, so any lingering online=true flags
     * are stale (e.g. left over from a crash or ungraceful shutdown). Clear them.
     */
    public void resetAllOffline() {
        try {
            for (UserPresence presence : presenceRepository.findAll()) {
                if (presence.isOnline()) {
                    presence.setOnline(false);
                    presenceRepository.save(presence);
                }
            }
        } catch (Exception e) {
            System.err.println("PresenceService: failed to reset presence on startup: " + e.getMessage());
        }
    }
}
