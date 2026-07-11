/**
 * Dashboard Role Guard
 *
 * Include AFTER clerk-auth.js on any admin/client/freelancer dashboard page.
 * Verifies the signed-in user's role via the backend and redirects away
 * if they don't hold the role required for that dashboard.
 *
 * Usage: <script src="dashboard-guard.js" data-require-role="ADMIN"></script>
 */
(function () {
    'use strict';

    const currentScript = document.currentScript;
    const requiredRole = currentScript.getAttribute('data-require-role');

    const ROLE_HOME = {
        ADMIN: 'admin-dashboard.html',
        CLIENT: 'client-dashboard.html',
        FREELANCER: 'freelancer-dashboard.html'
    };

    (async function guard() {
        try {
            const session = await window.NeonAuth.getSession();
            if (!session || !session.user) {
                window.location.href = 'login.html';
                return;
            }

            const syncResult = await window.NeonAuth.syncWithBackend(null);

            if (syncResult.role !== requiredRole && syncResult.role !== 'ADMIN') {
                window.location.href = ROLE_HOME[syncResult.role] || 'login.html';
                return;
            }

            // Session is valid — open the app-wide presence socket so other users
            // see this user as "online" while any dashboard page is open. The socket
            // closing (logout/tab close/network drop) is what marks them offline.
            loadPresenceSocket();
        } catch (e) {
            console.error('Dashboard guard failed:', e);
            window.location.href = 'login.html';
        }
    })();

    function loadPresenceSocket() {
        if (window.__presenceSocketLoaded) return;
        window.__presenceSocketLoaded = true;
        const s = document.createElement('script');
        s.src = 'presence-socket.js';
        s.async = true;
        document.head.appendChild(s);
    }
})();
