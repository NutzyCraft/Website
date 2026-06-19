/**
 * NutzyCraft Neon Auth Integration
 * 
 * Central authentication module using Neon Auth (Better Auth).
 * Provides: sign-in, sign-up, sign-out, session management, 
 * and a global fetch interceptor that auto-injects Bearer tokens.
 * 
 * Include this file AFTER config.js on any page that needs authentication.
 */
(function () {
    'use strict';

    const NEON_AUTH_URL = window.API_CONFIG.NEON_AUTH_URL;
    const API_BASE_URL = window.API_CONFIG.BASE_URL;

    // ─── Token Storage ────────────────────────────────────────────
    const TOKEN_KEY = 'neon_auth_token';
    const SESSION_KEY = 'neon_auth_session';

    function getStoredToken() {
        return localStorage.getItem(TOKEN_KEY) || sessionStorage.getItem(TOKEN_KEY);
    }

    function storeToken(token, remember) {
        if (remember) {
            localStorage.setItem(TOKEN_KEY, token);
            sessionStorage.removeItem(TOKEN_KEY);
        } else {
            sessionStorage.setItem(TOKEN_KEY, token);
            localStorage.removeItem(TOKEN_KEY);
        }
    }

    function clearToken() {
        localStorage.removeItem(TOKEN_KEY);
        sessionStorage.removeItem(TOKEN_KEY);
        localStorage.removeItem(SESSION_KEY);
        sessionStorage.removeItem(SESSION_KEY);
        localStorage.removeItem('loggedInEmail');
        sessionStorage.removeItem('loggedInEmail');
    }

    // ─── Better Auth API Calls ─────────────────────────────────────

    /**
     * Sign in with email and password via Neon Auth.
     * @returns {Promise<{token: string, user: object}>}
     */
    async function signInEmail(email, password) {
        const res = await fetch(`${NEON_AUTH_URL}/sign-in/email`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password }),
            credentials: 'include'
        });

        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || err.error || 'Sign-in failed');
        }

        const data = await res.json();

        // Extract token from response header or body
        const headerToken = res.headers.get('set-auth-token');
        const token = headerToken || data.token || null;

        if (token) {
            storeToken(token, true);
        }

        return { token, user: data.user || data, session: data.session };
    }

    /**
     * Sign up with email and password via Neon Auth.
     * @returns {Promise<{token: string, user: object}>}
     */
    async function signUpEmail(email, password, name) {
        const res = await fetch(`${NEON_AUTH_URL}/sign-up/email`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, password, name }),
            credentials: 'include'
        });

        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || err.error || 'Sign-up failed');
        }

        const data = await res.json();

        const headerToken = res.headers.get('set-auth-token');
        const token = headerToken || data.token || null;

        if (token) {
            storeToken(token, true);
        }

        return { token, user: data.user || data, session: data.session };
    }

    /**
     * Verify Email OTP
     * @param {string} email
     * @param {string} otp
     */
    async function verifyEmailOtp(email, otp) {
        const res = await fetch(`${NEON_AUTH_URL}/email-otp/verify-email`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, otp, type: 'email-verification' }),
        });
        const data = await res.json();
        if (!res.ok) {
            throw new Error(data.message || data.error || 'Failed to verify OTP');
        }
        
        const headerToken = res.headers.get('set-auth-token');
        const token = headerToken || data.token || null;
        if (token) {
            storeToken(token, true);
        }
        return { token, user: data.user || data };
    }

    /**
     * Resend Email OTP
     * @param {string} email
     */
    async function resendVerificationOtp(email) {
        const res = await fetch(`${NEON_AUTH_URL}/email-otp/send-verification-otp`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ email, type: 'email-verification' }),
        });
        const data = await res.json();
        if (!res.ok) {
            throw new Error(data.message || data.error || 'Failed to resend OTP');
        }
        return data;
    }

    /**
     * Sign in via Google OAuth.
     * @param {string} callbackURL - Where to redirect after Google OAuth
     */
    async function signInGoogle(callbackURL) {
        try {
            const res = await fetch(`${NEON_AUTH_URL}/sign-in/social`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    provider: 'google',
                    callbackURL: callbackURL || window.location.href
                })
            });
            const data = await res.json();
            if (data.url) {
                window.location.href = data.url;
            } else {
                console.error('Failed to get Google OAuth URL', data);
            }
        } catch (e) {
            console.error('Failed to sign in with Google', e);
        }
    }

    /**
     * Get the current session from Neon Auth.
     * @returns {Promise<{session: object, user: object}|null>}
     */
    async function getSession() {
        try {
            const res = await fetch(`${NEON_AUTH_URL}/get-session`, {
                method: 'GET',
                credentials: 'include',
                headers: {
                    'Authorization': getStoredToken() ? `Bearer ${getStoredToken()}` : ''
                }
            });

            if (!res.ok) return null;

            const data = await res.json().catch(() => null);
            if (data && data.session) {
                // Update stored token if a new one is issued
                const headerToken = res.headers.get('set-auth-token');
                const bodyToken = data.session.token || null;
                const tokenToStore = headerToken || bodyToken;
                
                if (tokenToStore) {
                    storeToken(tokenToStore, true);
                }
                return data;
            }
            return null;
        } catch (e) {
            console.error('Failed to get session:', e);
            return null;
        }
    }

    /**
     * Sign out from Neon Auth and clear local storage.
     */
    async function signOut() {
        try {
            await fetch(`${NEON_AUTH_URL}/sign-out`, {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Authorization': getStoredToken() ? `Bearer ${getStoredToken()}` : ''
                }
            });
        } catch (e) {
            console.warn('Sign-out request failed (session may already be expired):', e);
        }
        clearToken();
    }

    /**
     * Check if user is authenticated (has a stored token).
     */
    function isAuthenticated() {
        return !!getStoredToken();
    }

    // ─── Backend Sync ─────────────────────────────────────────────

    /**
     * Sync the Neon Auth user with the NutzyCraft backend.
     * Creates or links the user record on first sign-in.
     * 
     * @param {string} role - "CLIENT" or "FREELANCER" (for first-time users)
     * @returns {Promise<{id, email, fullName, role, isNew}>}
     */
    async function syncWithBackend(role) {
        const token = getStoredToken();
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
     * 
     * This is the LIGHTWEIGHT GLOBAL INTERCEPTOR requested by the user.
     * It ensures every existing fetch() call across all dashboard/page files
     * will seamlessly include the Bearer token without manual refactoring.
     * 
     * Rules:
     * - Only injects on requests to the API_BASE_URL
     * - Does NOT inject on requests to external services (Neon Auth, Google, etc.)
     * - Does NOT override an existing Authorization header if one is present
     */
    const _originalFetch = window.fetch;

    window.fetch = function (input, init) {
        const url = (typeof input === 'string') ? input : (input instanceof Request ? input.url : String(input));

        // Only intercept requests to our backend API
        if (url.startsWith(API_BASE_URL) || url.startsWith('/api/')) {
            const token = getStoredToken();
            if (token) {
                init = init || {};
                init.headers = init.headers || {};

                // Handle Headers object, array, or plain object
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
                    // Plain object
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
        signInEmail,
        signUpEmail,
        verifyEmailOtp,
        resendVerificationOtp,
        signInGoogle,
        getSession,
        signOut,
        isAuthenticated,
        syncWithBackend,
        getStoredToken,
        storeToken,
        clearToken
    };

})();
