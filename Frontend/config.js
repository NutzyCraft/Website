// API Configuration
// Change this URL when deploying to production
const API_CONFIG = {
  // Automatically detect development vs production environment
  BASE_URL: window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1'
      ? 'http://localhost:8080'
      : 'https://nutzy-craft-4qhbx.ondigitalocean.app',

  // Neon Auth base URL for authentication
  NEON_AUTH_URL: 'https://ep-snowy-hat-a1duupft.neonauth.ap-southeast-1.aws.neon.tech/neondb/auth'
};

// Export for use in other files
window.API_CONFIG = API_CONFIG;
