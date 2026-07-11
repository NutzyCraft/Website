/**
 * App-wide presence socket.
 *
 * Loaded by dashboard-guard.js on every authenticated dashboard page. Opens a
 * single STOMP/WebSocket connection whose sole purpose is presence: the backend
 * marks the user "online" on connect and "offline" when the last connection
 * closes (see PresenceService). There are no periodic heartbeat writes.
 *
 * The messages pages keep their own socket for message delivery — a user simply
 * holds two sessions there, which the backend's per-user session counting handles.
 */
(function () {
    'use strict';

    if (window.__presenceSocketStarted) return;
    window.__presenceSocketStarted = true;

    const SOCKJS_SRC = 'https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js';
    const STOMP_SRC = 'https://cdn.jsdelivr.net/npm/@stomp/stompjs@6.1.2/bundles/stomp.umd.min.js';

    function loadScript(src) {
        return new Promise(function (resolve, reject) {
            // Reuse an existing tag if the page already includes this library.
            const existing = document.querySelector('script[src="' + src + '"]');
            if (existing) {
                if (existing.dataset.loaded === 'true') return resolve();
                existing.addEventListener('load', resolve);
                existing.addEventListener('error', reject);
                return;
            }
            const s = document.createElement('script');
            s.src = src;
            s.onload = function () { s.dataset.loaded = 'true'; resolve(); };
            s.onerror = reject;
            document.head.appendChild(s);
        });
    }

    async function ensureLibs() {
        if (typeof window.SockJS === 'undefined') await loadScript(SOCKJS_SRC);
        if (typeof window.StompJs === 'undefined') await loadScript(STOMP_SRC);
    }

    async function start() {
        try {
            await ensureLibs();
            if (typeof window.StompJs === 'undefined' || typeof window.SockJS === 'undefined') return;
            if (!window.NeonAuth || !window.API_CONFIG) return;

            const token = await window.NeonAuth.getStoredToken();
            if (!token) return;

            const base = window.API_CONFIG.BASE_URL;
            const client = new window.StompJs.Client({
                webSocketFactory: function () { return new window.SockJS(base + '/ws'); },
                connectHeaders: { Authorization: 'Bearer ' + token },
                // Match the server broker heartbeat so dead connections are noticed quickly.
                heartbeatIncoming: 10000,
                heartbeatOutgoing: 10000,
                reconnectDelay: 5000
            });

            window.__presenceClient = client;
            client.activate();

            // Send a clean STOMP DISCONNECT on navigation/close so the user goes
            // offline promptly instead of waiting for the heartbeat timeout.
            window.addEventListener('pagehide', function () {
                try { client.deactivate(); } catch (e) { /* ignore */ }
            });
        } catch (e) {
            // Presence is best-effort; never let it break the page.
            console.debug('presence-socket: not started —', e);
        }
    }

    start();
})();
