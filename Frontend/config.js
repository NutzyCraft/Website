// API Configuration
// Change this URL when deploying to production
// Automatically detect development vs production environment
// Set USE_LOCAL_BACKEND to true only if running a local Spring Boot backend on localhost:8080 with dev Clerk keys
const USE_LOCAL_BACKEND = false;

// Check if running on Vercel preview deployment (typically ends in .vercel.app)
const IS_VERCEL_PREVIEW = typeof window !== 'undefined' && window.location.hostname.includes('vercel.app');

const API_CONFIG = {
  BASE_URL: USE_LOCAL_BACKEND
      ? 'http://localhost:8080'
      : IS_VERCEL_PREVIEW
          ? 'https://nutzycraft-backend-dev-33f88538c185.herokuapp.com'
          : 'https://nutzycraft-backend-f3be771b347a.herokuapp.com',

  // Clerk Configuration
  CLERK_PUBLISHABLE_KEY: (USE_LOCAL_BACKEND || IS_VERCEL_PREVIEW)
      ? 'pk_test_Y29ycmVjdC1zbmlwZS02Ny5jbGVyay5hY2NvdW50cy5kZXYk'
      : 'pk_live_Y2xlcmsubnV0enljcmFmdC5jb20k',
  CLERK_FRONTEND_API_URL: (USE_LOCAL_BACKEND || IS_VERCEL_PREVIEW)
      ? 'https://correct-snipe-67.clerk.accounts.dev'
      : 'https://clerk.nutzycraft.com'
};

// Export for use in other files
window.API_CONFIG = API_CONFIG;