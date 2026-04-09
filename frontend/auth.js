const TOKEN_KEY = 'fintrack-token';
const SESSION_KEY = 'fintrack-session';

const API_BASE = window.location.protocol.startsWith('http')
  ? `${window.location.protocol}//${window.location.hostname || 'localhost'}:8080/api`
  : 'http://localhost:8080/api';

function setStatus(message, ok = true) {
  const statusEl = document.getElementById('authStatus');
  if (!statusEl) return;
  statusEl.textContent = message;
  statusEl.className = `auth-status ${ok ? 'ok' : 'err'}`;
}

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE}${path}`, {
    headers: {
      'Content-Type': 'application/json',
      ...(options.headers || {}),
    },
    ...options,
  });

  const text = await response.text();
  let payload;
  try {
    payload = text ? JSON.parse(text) : {};
  } catch {
    payload = { message: text };
  }

  if (!response.ok) {
    throw new Error(payload?.message || payload?.error || `Request failed (${response.status})`);
  }

  return payload;
}

function storeSession(authResponse) {
  localStorage.setItem(TOKEN_KEY, authResponse.accessToken);
  localStorage.setItem(SESSION_KEY, JSON.stringify(authResponse.user));
}

function val(id) {
  return document.getElementById(id)?.value?.trim() || '';
}

async function onRegisterSubmit(event) {
  event.preventDefault();
  try {
    setStatus('Creating your account...');
    await request('/auth/register', {
      method: 'POST',
      body: JSON.stringify({
        username: val('username'),
        email: val('email'),
        password: val('password'),
        fullName: val('fullName'),
      }),
    });
    setStatus('Account created successfully. Redirecting to user login...', true);
    setTimeout(() => {
      window.location.href = 'user-login.html';
    }, 1000);
  } catch (error) {
    setStatus(error instanceof Error ? error.message : 'Failed to create account', false);
  }
}

async function onRequestOtpSubmit(event, mode) {
  event.preventDefault();
  try {
    setStatus('Sending OTP to your email...');
    const response = await request(`/auth/${mode}/request-otp`, {
      method: 'POST',
      body: JSON.stringify({
        email: val('email'),
      }),
    });

    const otpSection = document.getElementById('otpSection');
    if (otpSection) otpSection.hidden = false;

    const expiresAt = response?.expiresAt ? ` OTP valid until ${new Date(response.expiresAt).toLocaleTimeString()}.` : '';
    setStatus(`OTP sent successfully.${expiresAt}`, true);
  } catch (error) {
    setStatus(error instanceof Error ? error.message : 'Failed to request OTP', false);
  }
}

async function onDirectLoginSubmit(event, mode) {
  event.preventDefault();
  try {
    setStatus('Signing in with password...');
    const authResponse = await request(`/auth/${mode}/login`, {
      method: 'POST',
      body: JSON.stringify({
        email: val('email'),
        password: val('password'),
      }),
    });

    storeSession(authResponse);
    setStatus('Login successful. Redirecting to workspace...', true);
    setTimeout(() => {
      window.location.href = 'users.html';
    }, 500);
  } catch (error) {
    setStatus(error instanceof Error ? error.message : 'Direct login failed', false);
  }
}

async function onVerifyOtpSubmit(event, mode) {
  event.preventDefault();
  try {
    setStatus('Validating OTP and signing you in...');
    const authResponse = await request(`/auth/${mode}/verify-otp`, {
      method: 'POST',
      body: JSON.stringify({
        email: val('email'),
        otp: val('otp'),
      }),
    });

    storeSession(authResponse);
    setStatus('Login successful. Redirecting to workspace...', true);
    setTimeout(() => {
      window.location.href = 'users.html';
    }, 700);
  } catch (error) {
    setStatus(error instanceof Error ? error.message : 'OTP verification failed', false);
  }
}

function bindRegisterPage() {
  const form = document.getElementById('registerForm');
  if (!form) return;
  form.addEventListener('submit', onRegisterSubmit);
}

function bindLoginPage(mode) {
  const requestForm = document.getElementById('requestOtpForm');
  const verifyForm = document.getElementById('verifyOtpForm');
  const directLoginButton = document.getElementById('directLoginButton');
  if (!requestForm || !verifyForm || !directLoginButton) return;

  requestForm.addEventListener('submit', event => onRequestOtpSubmit(event, mode));
  verifyForm.addEventListener('submit', event => onVerifyOtpSubmit(event, mode));
  directLoginButton.addEventListener('click', event => onDirectLoginSubmit(event, mode));
}

document.addEventListener('DOMContentLoaded', () => {
  const page = document.body.dataset.authPage;
  if (page === 'register') {
    bindRegisterPage();
    return;
  }

  if (page === 'user-login') {
    bindLoginPage('user');
    return;
  }

  if (page === 'admin-login') {
    bindLoginPage('admin');
  }
});
