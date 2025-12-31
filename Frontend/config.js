// API Configuration
// Change this URL when deploying to production
const API_CONFIG = {
  // For local development
  // BASE_URL: 'http://localhost:8080'

  // For Render deployment (update after you get your Render URL)
  BASE_URL: "https://nutzycraft-backend.onrender.com",

  // Uncomment the line below for automatic detection
  // BASE_URL: window.location.hostname === 'localhost'
  //     ? 'http://localhost:8080'
  //     : 'https://nutzycraft-backend.onrender.com'
};

// Export for use in other files
window.API_CONFIG = API_CONFIG;
