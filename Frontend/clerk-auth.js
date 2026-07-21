/**
 * NutzyCraft Clerk Auth Integration
 *
 * Central authentication module using Clerk.
 * Loads the Clerk JS SDK, exposes sign-in/sign-up/sign-out/session helpers,
 * and a global fetch interceptor that auto-injects Bearer tokens.
 *
 * Include this file AFTER config.js on any page that needs authentication.
 * Exposes the same window.NeonAuth-shaped API so existing pages don't need
 * to change how they call into auth.
 */
(function () {
    'use strict';

    const CLERK_PUBLISHABLE_KEY = window.API_CONFIG.CLERK_PUBLISHABLE_KEY;
    const CLERK_FRONTEND_API_URL = window.API_CONFIG.CLERK_FRONTEND_API_URL;
    const API_BASE_URL = window.API_CONFIG.BASE_URL;

    // ─── Load the Clerk SDK ─────────────────────────────────────────
    let clerkReadyPromise = null;

    function loadClerk() {
        if (clerkReadyPromise) return clerkReadyPromise;

        clerkReadyPromise = new Promise((resolve, reject) => {
            const script = document.createElement('script');
            script.async = true;
            script.crossOrigin = 'anonymous';
            script.setAttribute('data-clerk-publishable-key', CLERK_PUBLISHABLE_KEY);
            script.src = 'https://cdn.jsdelivr.net/npm/@clerk/clerk-js@latest/dist/clerk.browser.js';

            script.onload = async () => {
                try {
                    await window.Clerk.load({
                        publishableKey: CLERK_PUBLISHABLE_KEY
                    });
                    resolve(window.Clerk);
                } catch (e) {
                    reject(e);
                }
            };
            script.onerror = () => reject(new Error('Failed to load Clerk SDK'));

            document.head.appendChild(script);
        });

        return clerkReadyPromise;
    }

    // Kick off loading immediately so Clerk is ready by the time pages need it.
    const clerkLoading = loadClerk();

    // ─── Token / Session Helpers ─────────────────────────────────────

    async function getStoredToken() {
        const clerk = await clerkLoading;
        if (!clerk.session) return null;
        try {
            // Clerk session tokens are short-lived (~60s) and auto-refreshed;
            // getToken() returns a cached-but-fresh token, refreshing only when needed.
            return await clerk.session.getToken();
        } catch (e) {
            return null;
        }
    }

    async function isAuthenticated() {
        const clerk = await clerkLoading;
        return !!clerk.session;
    }

    async function getSession() {
        const clerk = await clerkLoading;
        if (!clerk.session || !clerk.user) return null;
        const token = await getStoredToken();
        const activeEmail = clerk.user.primaryEmailAddress ? clerk.user.primaryEmailAddress.emailAddress : null;
        if (activeEmail) {
            localStorage.setItem('loggedInEmail', activeEmail);
            sessionStorage.setItem('loggedInEmail', activeEmail);
        }
        return {
            token,
            session: clerk.session,
            user: {
                id: clerk.user.id,
                email: activeEmail,
                name: clerk.user.fullName
            }
        };
    }

    async function signOut() {
        const clerk = await clerkLoading;
        try {
            await clerk.signOut();
        } catch (e) {
            console.warn('Sign-out request failed:', e);
        }
        cachedToken = null;
        localStorage.removeItem('loggedInEmail');
        sessionStorage.removeItem('loggedInEmail');
    }

    // ─── Backend Sync ─────────────────────────────────────────────

    /**
     * Sync the Clerk user with the NutzyCraft backend.
     * Creates or links the user record on first sign-in.
     *
     * @param {string} role - "CLIENT" or "FREELANCER" (for first-time users)
     * @returns {Promise<{id, email, fullName, role, isNew}>}
     */
    async function syncWithBackend(role) {
        const token = await getStoredToken();
        if (!token) {
            throw new Error('No auth token available for sync');
        }

        const res = await fetch(`${API_BASE_URL}/api/auth/sync`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'Authorization': `Bearer ${token}`
            },
            body: JSON.stringify({ role: role || null })
        });

        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || err.error || 'Backend sync failed');
        }

        return await res.json();
    }

    // ─── Global Fetch Interceptor ──────────────────────────────────

    /**
     * Override window.fetch to automatically inject the Authorization header
     * on all requests to the NutzyCraft API backend.
     */
    const _originalFetch = window.fetch;

    window.fetch = async function (input, init) {
        const url = (typeof input === 'string') ? input : (input instanceof Request ? input.url : String(input));

        if (url.startsWith(API_BASE_URL) || url.startsWith('/api/')) {
            const token = await getStoredToken();
            if (token) {
                init = init || {};
                init.headers = init.headers || {};

                if (init.headers instanceof Headers) {
                    if (!init.headers.has('Authorization')) {
                        init.headers.set('Authorization', `Bearer ${token}`);
                    }
                } else if (Array.isArray(init.headers)) {
                    const hasAuth = init.headers.some(([key]) => key.toLowerCase() === 'authorization');
                    if (!hasAuth) {
                        init.headers.push(['Authorization', `Bearer ${token}`]);
                    }
                } else {
                    if (!init.headers['Authorization'] && !init.headers['authorization']) {
                        init.headers['Authorization'] = `Bearer ${token}`;
                    }
                }
            }
        }

        return _originalFetch.call(window, input, init);
    };

    // ─── Public API ───────────────────────────────────────────────

    window.NeonAuth = {
        clerkLoading,
        getSession,
        signOut,
        isAuthenticated,
        syncWithBackend,
        getStoredToken
    };

})();
