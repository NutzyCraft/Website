// API Configuration
// Change this URL when deploying to production
// Automatically detect development vs production environment
const IS_LOCAL = window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1';

const API_CONFIG = {
  BASE_URL: IS_LOCAL
      ? 'http://localhost:8080'
      : 'https://nutzycraft-backend-pynth.ondigitalocean.app',

  // Clerk Configuration
  // Local dev uses the Clerk development instance; production uses the live instance.
  CLERK_PUBLISHABLE_KEY: IS_LOCAL
      ? 'pk_test_Y29ycmVjdC1zbmlwZS02Ny5jbGVyay5hY2NvdW50cy5kZXYk'
      : 'pk_live_Y2xlcmsubnV0enljcmFmdC5jb20k',
  CLERK_FRONTEND_API_URL: IS_LOCAL
      ? 'https://correct-snipe-67.clerk.accounts.dev'
      : 'https://clerk.nutzycraft.com'
};

// Export for use in other files
window.API_CONFIG = API_CONFIG;