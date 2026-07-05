// API Configuration
// Change this URL when deploying to production
const API_CONFIG = {
  // Automatically detect development vs production environment
  BASE_URL: window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'
      ? 'http://localhost:8080'
      : 'https://nutzy-backend-dev-gefkm.ondigitalocean.app',

  // Clerk Configuration
  CLERK_PUBLISHABLE_KEY: 'pk_test_Y29ycmVjdC1zbmlwZS02Ny5jbGVyay5hY2NvdW50cy5kZXYk',
  CLERK_FRONTEND_API_URL: 'https://correct-snipe-67.clerk.accounts.dev'
};

// Export for use in other files
window.API_CONFIG = API_CONFIG;