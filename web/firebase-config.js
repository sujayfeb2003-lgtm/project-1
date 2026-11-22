/**
 * Firebase Web Configuration
 * Replace the configuration values below with your Firebase project keys from:
 * Firebase Console -> Project Settings -> General -> Your Apps -> Web App
 */
export const firebaseConfig = {
  apiKey: "AIzaSyYOUR_API_KEY_HERE",
  authDomain: "skillstream-auth.firebaseapp.com",
  projectId: "skillstream-auth",
  storageBucket: "skillstream-auth.appspot.com",
  messagingSenderId: "123456789012",
  appId: "1:123456789012:web:abcdef123456789"
};

/**
 * Action Code Settings for Passwordless Authentication.
 * Configures the redirect URL, mobile app handlers, and iOS/Android deep linking.
 */
export const actionCodeSettings = {
  // URL to redirect back to. The domain must be whitelisted in Firebase Console -> Auth -> Authorized Domains
  url: window.location.origin + window.location.pathname,
  // This must be true for email link sign-in to complete in web/apps
  handleCodeInApp: true,
  // iOS configuration (optional, if user clicks email on iPhone)
  iOS: {
    bundleId: 'com.aistudio.skillstream'
  },
  // Android configuration (optional, if user clicks email on Android)
  android: {
    packageName: 'com.aistudio.skillstream.kxmpzq',
    installApp: true,
    minimumVersion: '1'
  },
  // Optional Dynamic Link domain if using custom FDL
  // dynamicLinkDomain: 'skillstream.page.link'
};
