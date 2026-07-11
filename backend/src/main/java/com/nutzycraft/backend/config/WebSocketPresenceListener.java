package com.nutzycraft.backend.config;

import com.nutzycraft.backend.service.PresenceService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.security.Principal;

/**
 * Bridges Spring's WebSocket session lifecycle events to {@link PresenceService}.
 * The STOMP CONNECT is authenticated in {@code WebSocketSecurityConfig}, which
 * sets the session Principal to the user's email — so these events identify the user.
 */
@Component
public class WebSocketPresenceListener {

    private final PresenceService presenceService;

    public WebSocketPresenceListener(PresenceService presenceService) {
        this.presenceService = presenceService;
    }

    @EventListener
    public void onSessionConnected(SessionConnectedEvent event) {
        Principal user = event.getUser();
        if (user == null) return;
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        presenceService.onConnect(user.getName(), accessor.getSessionId());
    }

    @EventListener
    public void onSessionDisconnect(SessionDisconnectEvent event) {
        Principal user = event.getUser();
        if (user == null) return;
        presenceService.onDisconnect(user.getName(), event.getSessionId());
    }

    @EventListener(ApplicationReadyEvent.class)
    public void clearStalePresenceOnStartup() {
        presenceService.resetAllOffline();
    }
}
