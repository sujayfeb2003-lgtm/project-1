/**
 * Firebase Authentication - Web Implementation
 * Supports Email ID & Password (Sign In / Sign Up / Password Reset)
 * and Passwordless Email Link Authentication using Firebase Modular SDK (v10).
 */
import { initializeApp } from 'https://www.gstatic.com/firebasejs/10.8.0/firebase-app.js';
import {
  getAuth,
  signInWithEmailAndPassword,
  createUserWithEmailAndPassword,
  sendPasswordResetEmail,
  sendSignInLinkToEmail,
  isSignInWithEmailLink,
  signInWithEmailLink,
  onAuthStateChanged,
  signOut
} from 'https://www.gstatic.com/firebasejs/10.8.0/firebase-auth.js';

import { firebaseConfig, actionCodeSettings } from './firebase-config.js';

// Initialize Firebase
const app = initializeApp(firebaseConfig);
const auth = getAuth(app);

// DOM Elements
const authFormContainer = document.getElementById('auth-form-container');
const userProfileContainer = document.getElementById('user-profile-container');
const tabPassword = document.getElementById('tab-password');
const tabMagicLink = document.getElementById('tab-magic-link');

const passwordAuthForm = document.getElementById('password-auth-form');
const pwEmailInput = document.getElementById('pw-email-input');
const pwPasswordInput = document.getElementById('pw-password-input');
const pwConfirmGroup = document.getElementById('confirm-password-group');
const pwConfirmInput = document.getElementById('pw-confirm-input');
const pwSubmitBtn = document.getElementById('pw-submit-btn');
const toggleSignupBtn = document.getElementById('toggle-signup-btn');
const forgotPasswordBtn = document.getElementById('forgot-password-btn');

const authForm = document.getElementById('auth-form');
const emailInput = document.getElementById('email-input');
const sendLinkBtn = document.getElementById('send-link-btn');
const signOutBtn = document.getElementById('sign-out-btn');
const alertBox = document.getElementById('alert-box');
const profileEmail = document.getElementById('profile-email');
const profileUid = document.getElementById('profile-uid');
const avatarInitials = document.getElementById('avatar-initials');
const authMethodLabel = document.getElementById('auth-method-label');
const subheading = document.getElementById('subheading');

const STORAGE_KEY_EMAIL = 'emailForSignIn';
let isSignUpMode = false;

function showAlert(message, type = 'info') {
  alertBox.className = `alert alert-${type}`;
  alertBox.textContent = message;
  alertBox.style.display = 'block';
}

function hideAlert() {
  alertBox.style.display = 'none';
}

function switchTab(mode) {
  hideAlert();
  if (mode === 'password') {
    tabPassword.classList.add('active');
    tabMagicLink.classList.remove('active');
    passwordAuthForm.style.display = 'block';
    authForm.style.display = 'none';
  } else {
    tabMagicLink.classList.add('active');
    tabPassword.classList.remove('active');
    passwordAuthForm.style.display = 'none';
    authForm.style.display = 'block';
  }
}

function updateSignUpMode(signUp) {
  isSignUpMode = signUp;
  hideAlert();
  if (isSignUpMode) {
    pwConfirmGroup.style.display = 'block';
    pwConfirmInput.required = true;
    pwSubmitBtn.innerHTML = '<span>Create Account</span>';
    toggleSignupBtn.textContent = 'Already have an account?';
    forgotPasswordBtn.style.display = 'none';
  } else {
    pwConfirmGroup.style.display = 'none';
    pwConfirmInput.required = false;
    pwSubmitBtn.innerHTML = '<span>Sign In with Email</span>';
    toggleSignupBtn.textContent = 'Create new account';
    forgotPasswordBtn.style.display = 'inline';
  }
}

/**
 * Email ID & Password Sign-In / Sign-Up
 */
async function handlePasswordAuthSubmit(event) {
  event.preventDefault();
  const email = pwEmailInput.value.trim();
  const password = pwPasswordInput.value;

  if (!email) {
    showAlert('Please enter a valid email address.', 'error');
    return;
  }

  if (isSignUpMode) {
    if (password.length < 6) {
      showAlert('Password must be at least 6 characters long.', 'error');
      return;
    }
    if (password !== pwConfirmInput.value) {
      showAlert('Passwords do not match.', 'error');
      return;
    }
  } else if (!password) {
    showAlert('Please enter your password.', 'error');
    return;
  }

  pwSubmitBtn.disabled = true;
  pwSubmitBtn.innerHTML = `<div class="spinner"></div><span>${isSignUpMode ? 'Creating Account...' : 'Signing In...'}</span>`;
  hideAlert();

  try {
    if (isSignUpMode) {
      const result = await createUserWithEmailAndPassword(auth, email, password);
      showAlert(`Account created for ${result.user.email}!`, 'success');
    } else {
      const result = await signInWithEmailAndPassword(auth, email, password);
      showAlert(`Welcome back, ${result.user.email}!`, 'success');
    }
    pwPasswordInput.value = '';
    pwConfirmInput.value = '';
  } catch (error) {
    console.error('Email/password auth failed:', error);
    showAlert(`Authentication failed: ${error.message}`, 'error');
  } finally {
    pwSubmitBtn.disabled = false;
    pwSubmitBtn.innerHTML = `<span>${isSignUpMode ? 'Create Account' : 'Sign In with Email'}</span>`;
  }
}

/**
 * Forgot Password -> Send Password Reset Email
 */
async function handleForgotPassword() {
  const email = pwEmailInput.value.trim();
  if (!email) {
    showAlert('Enter your email ID above first to reset your password.', 'error');
    return;
  }

  hideAlert();
  try {
    await sendPasswordResetEmail(auth, email);
    showAlert(`Password reset email sent to ${email}. Check your inbox.`, 'success');
  } catch (error) {
    console.error('Password reset error:', error);
    showAlert(`Could not send reset email: ${error.message}`, 'error');
  }
}

/**
 * Send Passwordless Magic Link to User's Email
 */
async function handleSendSignInLink(event) {
  event.preventDefault();
  const email = emailInput.value.trim();

  if (!email) {
    showAlert('Please enter a valid email address.', 'error');
    return;
  }

  sendLinkBtn.disabled = true;
  sendLinkBtn.innerHTML = '<div class="spinner"></div><span>Sending Link...</span>';
  hideAlert();

  try {
    await sendSignInLinkToEmail(auth, email, actionCodeSettings);
    window.localStorage.setItem(STORAGE_KEY_EMAIL, email);

    showAlert(`Magic sign-in link sent to ${email}! Check your inbox or spam folder and click the link to sign in.`, 'success');
    sendLinkBtn.innerHTML = '<span>Link Sent! Send Again?</span>';
    sendLinkBtn.disabled = false;
  } catch (error) {
    console.error('Error sending email link:', error);
    showAlert(`Error: ${error.message || 'Failed to send sign-in link.'}`, 'error');
    sendLinkBtn.innerHTML = '<span>Send Magic Link</span>';
    sendLinkBtn.disabled = false;
  }
}

/**
 * Handle Incoming Email Link and Complete Sign-In
 */
async function handleIncomingEmailLink() {
  const currentUrl = window.location.href;

  if (isSignInWithEmailLink(auth, currentUrl)) {
    switchTab('magic-link');
    showAlert('Verifying your magic sign-in link...', 'info');

    let email = window.localStorage.getItem(STORAGE_KEY_EMAIL);
    if (!email) {
      email = window.prompt('Please confirm your email address to complete passwordless sign-in:');
    }

    if (!email) {
      showAlert('Sign-in cancelled. Email address is required to complete verification.', 'error');
      return;
    }

    try {
      const result = await signInWithEmailLink(auth, email.trim(), currentUrl);
      window.localStorage.removeItem(STORAGE_KEY_EMAIL);
      window.history.replaceState({}, document.title, window.location.pathname);

      showAlert(`Welcome back, ${result.user.email}! Successfully authenticated.`, 'success');
    } catch (error) {
      console.error('Error signing in with email link:', error);
      showAlert(`Authentication failed: ${error.message}`, 'error');
    }
  }
}

/**
 * Handle Sign Out
 */
async function handleSignOut() {
  try {
    await signOut(auth);
    window.localStorage.removeItem(STORAGE_KEY_EMAIL);
    showAlert('Signed out successfully.', 'info');
  } catch (error) {
    showAlert(`Error signing out: ${error.message}`, 'error');
  }
}

/**
 * Auth State Observer
 */
onAuthStateChanged(auth, (user) => {
  if (user) {
    authFormContainer.style.display = 'none';
    userProfileContainer.style.display = 'block';
    subheading.textContent = 'Account Authenticated';

    profileEmail.textContent = user.email || 'Authenticated User';
    profileUid.textContent = `UID: ${user.uid}`;

    const providers = (user.providerData || []).map((p) => p.providerId);
    authMethodLabel.textContent = providers.includes('password')
      ? '✓ Authenticated via Email & Password'
      : '✓ Authenticated via Firebase';

    const initials = user.email ? user.email.substring(0, 2).toUpperCase() : 'US';
    avatarInitials.textContent = initials;
  } else {
    authFormContainer.style.display = 'block';
    userProfileContainer.style.display = 'none';
    subheading.textContent = 'Sign in with Email & Password or Magic Link';
  }
});

// Event Listeners
tabPassword.addEventListener('click', () => switchTab('password'));
tabMagicLink.addEventListener('click', () => switchTab('magic-link'));
toggleSignupBtn.addEventListener('click', () => updateSignUpMode(!isSignUpMode));
forgotPasswordBtn.addEventListener('click', handleForgotPassword);
passwordAuthForm.addEventListener('submit', handlePasswordAuthSubmit);
authForm.addEventListener('submit', handleSendSignInLink);
signOutBtn.addEventListener('click', handleSignOut);

// Execute incoming link verification on page load
handleIncomingEmailLink();
