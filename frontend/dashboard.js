const TOKEN_KEY = 'fintrack-token';
const SESSION_KEY = 'fintrack-session';

let token = localStorage.getItem(TOKEN_KEY) || '';
let session = (() => {
  try {
    const raw = localStorage.getItem(SESSION_KEY);
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
})();

const API_BASE = window.location.protocol.startsWith('http')
  ? `${window.location.protocol}//${window.location.hostname || 'localhost'}:8080/api`
  : 'http://localhost:8080/api';

const statusLine = document.getElementById('statusLine');
const resultEl = document.getElementById('result');
const responseDrawerEl = document.getElementById('responseDrawer');
const toastStackEl = document.getElementById('toastStack');
const sessionInfoEl = document.getElementById('sessionInfo');
const welcomeTitleEl = document.getElementById('welcomeTitle');
const logoutButton = document.getElementById('logoutButton');
const profileIdEl = document.getElementById('profile-id');
const profileNameEl = document.getElementById('profile-name');
const profileEmailEl = document.getElementById('profile-email');
const profileFullNameEl = document.getElementById('profile-fullname');
const profileCreatedAtEl = document.getElementById('profile-created-at');
const editUsernameEl = document.getElementById('edit-username');
const editEmailEl = document.getElementById('edit-email');
const editFullNameEl = document.getElementById('edit-fullname');
const editPasswordEl = document.getElementById('edit-password');

const actionFeedback = {
  'update-user': { success: 'User profile updated successfully', error: 'Failed to update profile' },
  'create-bank-account': { success: 'Bank account added successfully', error: 'Failed to add bank account' },
  'get-bank-account': { success: 'Bank account loaded', error: 'Failed to fetch bank account' },
  'list-bank-accounts': { success: 'Bank accounts loaded', error: 'Failed to list bank accounts' },
  'search-bank-accounts': { success: 'Account search completed', error: 'Failed to search bank accounts' },
  'delete-bank-account': { success: 'Bank account deleted successfully', error: 'Failed to delete bank account' },
  'record-income': { success: 'Income recorded successfully', error: 'Failed to record income' },
  'record-expense': { success: 'Expense recorded successfully', error: 'Failed to record expense' },
  'record-atm': { success: 'ATM withdrawal recorded', error: 'Failed to record ATM withdrawal' },
  'record-bank-expense': { success: 'Bank expense recorded', error: 'Failed to record bank expense' },
  'list-transactions': { success: 'Transactions loaded', error: 'Failed to list transactions' },
  'get-transaction': { success: 'Transaction loaded', error: 'Failed to fetch transaction' },
  'transfer-funds': { success: 'Transfer completed successfully', error: 'Failed to transfer funds' },
  'report-bank-balances': { success: 'Bank balance report generated', error: 'Failed to generate bank balance report' },
  'report-monthly-expenses': { success: 'Monthly expense report generated', error: 'Failed to generate monthly expense report' },
  'report-category-expense': { success: 'Category report generated', error: 'Failed to generate category report' },
  'report-summary': { success: 'Income/expense summary generated', error: 'Failed to generate income/expense summary' },
  'admin-users': { success: 'Admin users loaded', error: 'Failed to load admin users' },
  'admin-activities': { success: 'Admin activities loaded', error: 'Failed to load admin activities' },
};

function getSuccessMessage(action, data) {
  return data?.message || actionFeedback[action]?.success || 'Action completed successfully';
}

function getErrorMessage(action, data, status) {
  return data?.message || data?.error || actionFeedback[action]?.error || `Request failed (${status})`;
}

function showToast(message, type = 'info') {
  if (!toastStackEl || !message) return;
  const item = document.createElement('div');
  item.className = `toast-item ${type}`;
  item.textContent = message;
  toastStackEl.appendChild(item);

  const removeTimer = setTimeout(() => {
    item.remove();
  }, 3200);

  item.addEventListener('click', () => {
    clearTimeout(removeTimer);
    item.remove();
  });
}

const toast = {
  success: message => showToast(message, 'success'),
  error: message => showToast(message, 'error'),
  info: message => showToast(message, 'info'),
};

window.toast = toast;

function baseUrl() {
  return API_BASE;
}

function currentToken() {
  return token || '';
}

function currentSession() {
  return session;
}

function currentUserId() {
  return currentSession()?.id ?? null;
}

function currentRole() {
  return currentSession()?.role ?? 'USER';
}

function isAdmin() {
  return currentRole() === 'ADMIN';
}

function logout(redirectTo = 'user-login.html') {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(SESSION_KEY);
  token = '';
  session = null;
  window.location.href = redirectTo;
}

function roleUserId(id, label) {
  if (isAdmin()) {
    return requireNumber(id, label);
  }
  const uid = currentUserId();
  if (!uid) {
    throw new Error('Unable to resolve current user session. Please login again.');
  }
  return uid;
}

function reportUserId(id, label) {
  return isAdmin() ? requireNumber(id, label) : currentUserId();
}

function applySessionInfo() {
  const active = currentSession();
  if (!active) return;

  if (sessionInfoEl) {
    sessionInfoEl.textContent = `Logged in as ${active.fullName} (${active.email}) • Role: ${active.role}`;
  }

  if (welcomeTitleEl) {
    welcomeTitleEl.textContent = `Welcome, ${active.fullName}`;
  }

  if (profileIdEl) {
    profileIdEl.textContent = String(active.id ?? '-');
  }

  if (profileNameEl) {
    profileNameEl.textContent = active.username || '-';
  }

  if (profileEmailEl) {
    profileEmailEl.textContent = active.email || '-';
  }

  if (profileFullNameEl) {
    profileFullNameEl.textContent = active.fullName || '-';
  }

  if (profileCreatedAtEl) {
    const createdDate = active.createdAt ? new Date(active.createdAt) : null;
    profileCreatedAtEl.textContent = createdDate && !Number.isNaN(createdDate.getTime())
      ? createdDate.toLocaleString()
      : (active.createdAt || '-');
  }

  if (editUsernameEl) {
    editUsernameEl.value = active.username || '';
  }

  if (editEmailEl) {
    editEmailEl.value = active.email || '';
  }

  if (editFullNameEl) {
    editFullNameEl.value = active.fullName || '';
  }

  if (editPasswordEl) {
    editPasswordEl.value = '';
  }
}

function applyRoleDefaults() {
  if (isAdmin()) return;

  const userIdInputs = [
    'ba-userId',
    'ba-user-list',
    'tx-user-list',
    'r-bank-userId',
    'r-monthly-userId',
    'r-category-userId',
    'r-summary-userId'
  ];

  userIdInputs.forEach(id => {
    const input = document.getElementById(id);
    if (!input) return;
    input.value = String(currentUserId() || '');
    input.setAttribute('readonly', 'readonly');
  });

  ['search-bank-accounts', 'admin-users', 'admin-activities'].forEach(action => {
    const button = document.querySelector(`[data-action="${action}"]`);
    if (!button) return;
    button.setAttribute('disabled', 'disabled');
    button.setAttribute('title', 'Admin access required');
  });
}

async function bootstrapSession() {
  if (!currentToken()) {
    logout('user-login.html');
    return;
  }

  try {
    const response = await fetch(`${baseUrl()}/auth/me`, {
      headers: {
        Authorization: `Bearer ${currentToken()}`,
      },
    });

    if (!response.ok) {
      throw new Error('Session expired');
    }

    const profile = await response.json();
    session = profile;
    localStorage.setItem(SESSION_KEY, JSON.stringify(profile));
    applySessionInfo();
    applyRoleDefaults();
  } catch {
    logout('user-login.html');
  }
}

function setStatus(message, ok = true) {
  if (!statusLine) return;
  statusLine.textContent = message;
  statusLine.className = `status-line ${ok ? 'ok' : 'err'}`;
}

function getInlineStatusNode(target) {
  const card = target.closest('.op-card, .card');
  if (!card) return null;

  let node = card.querySelector('[data-inline-status]');
  if (node) return node;

  node = document.createElement('p');
  node.className = 'inline-status';
  node.setAttribute('data-inline-status', 'true');

  const buttonRow = target.closest('.btn-row');
  if (buttonRow && buttonRow.parentElement === card) {
    buttonRow.insertAdjacentElement('afterend', node);
    return node;
  }

  if (target.parentElement === card) {
    target.insertAdjacentElement('afterend', node);
    return node;
  }

  card.appendChild(node);
  return node;
}

function setInlineStatus(target, message, type = 'info') {
  const node = getInlineStatusNode(target);
  if (!node) return;
  node.textContent = message;
  node.className = `inline-status ${type}`;
}

function val(id) {
  return document.getElementById(id)?.value?.trim() ?? '';
}

function num(id) {
  const raw = val(id);
  return raw === '' ? null : Number(raw);
}

function requireNumber(id, label) {
  const value = num(id);
  if (value === null || Number.isNaN(value) || value <= 0) {
    throw new Error(`${label} is required`);
  }
  return value;
}

function requireText(id, label) {
  const value = val(id);
  if (!value) {
    throw new Error(`${label} is required`);
  }
  return value;
}

function escapeHtml(input) {
  return String(input)
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');
}

function renderTable(items) {
  if (!Array.isArray(items) || !items.length) {
    return '<p class="result-empty">No records found.</p>';
  }

  const columns = Array.from(items.reduce((set, row) => {
    Object.keys(row || {}).forEach(key => set.add(key));
    return set;
  }, new Set()));

  return `
    <div class="result-table-wrap">
      <table class="result-table">
        <thead><tr>${columns.map(c => `<th>${escapeHtml(c)}</th>`).join('')}</tr></thead>
        <tbody>
          ${items.map(row => `
            <tr>${columns.map(c => `<td>${escapeHtml(row?.[c] ?? '')}</td>`).join('')}</tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `;
}

function renderKeyValue(data) {
  const entries = Object.entries(data || {});
  if (!entries.length) {
    return '<p class="result-empty">No details available.</p>';
  }

  return `
    <div class="kv-grid">
      ${entries.map(([key, value]) => {
        const printable = typeof value === 'object' && value !== null ? JSON.stringify(value) : String(value);
        return `<div class="kv-item"><div class="kv-key">${escapeHtml(key)}</div><div class="kv-val">${escapeHtml(printable)}</div></div>`;
      }).join('')}
    </div>
  `;
}

function renderResult(data) {
  if (!resultEl) return;
  const body = Array.isArray(data) ? renderTable(data) : renderKeyValue(data);
  resultEl.innerHTML = body;
}

async function api(path, options = {}, action = 'request') {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 20000);

  const headers = {
    'Content-Type': 'application/json',
    ...(options.headers || {}),
  };

  const bearer = currentToken();
  if (bearer) {
    headers.Authorization = `Bearer ${bearer}`;
  }

  let response;
  let raw;
  try {
    response = await fetch(`${baseUrl()}${path}`, { ...options, headers, signal: controller.signal });
    raw = await response.text();
  } catch (error) {
    clearTimeout(timeout);
    if (error.name === 'AbortError') {
      throw new Error('Request timed out. Check backend status and try again.');
    }
    throw new Error('Unable to reach backend. Check if the server is running.');
  }
  clearTimeout(timeout);

  let data;
  try {
    data = raw ? JSON.parse(raw) : { status: response.status, message: 'No content' };
  } catch {
    data = { status: response.status, message: raw };
  }

  if (!response.ok) {
    const message = response.status === 401
      ? 'Unauthorized. This action requires an authenticated session.'
      : getErrorMessage(action, data, response.status);

    toast.error(message);
    renderResult(data);
    if (responseDrawerEl) responseDrawerEl.open = true;
    const err = new Error(message);
    err.toastShown = true;
    throw err;
  }

  const successMessage = getSuccessMessage(action, data);
  toast.success(`✅ ${successMessage}`);
  renderResult(data);
  return data;
}

async function handleAction(action) {
  const run = (path, options = {}) => api(path, options, action);

  switch (action) {
    case 'update-user': {
      const userId = currentUserId();
      if (!userId) {
        throw new Error('Unable to resolve current user session. Please login again.');
      }

      const payload = {
        username: requireText('edit-username', 'Username'),
        email: requireText('edit-email', 'Email'),
        fullName: requireText('edit-fullname', 'Full name')
      };

      const newPassword = val('edit-password');
      if (newPassword) {
        payload.password = newPassword;
      }

      return run(`/users/${userId}`, {
        method: 'PUT',
        body: JSON.stringify(payload)
      }).then(data => {
        session = data;
        localStorage.setItem(SESSION_KEY, JSON.stringify(data));
        applySessionInfo();
        return data;
      });
    }

    case 'create-bank-account':
      return run('/bank-accounts', {
        method: 'POST',
        body: JSON.stringify({
          userId: roleUserId('ba-userId', 'User ID'),
          bankName: requireText('ba-bankName', 'Bank name'),
          accountNumber: requireText('ba-accountNumber', 'Account number'),
          initialBalance: requireNumber('ba-initialBalance', 'Initial balance'),
        }),
      });

    case 'get-bank-account':
      return run(`/bank-accounts/${requireNumber('ba-id', 'Account ID')}`);

    case 'list-bank-accounts':
      return run(`/bank-accounts/user/${roleUserId('ba-user-list', 'User ID')}`);

    case 'search-bank-accounts': {
      const params = new URLSearchParams();
      if (val('ba-search-bank')) params.set('bankName', val('ba-search-bank'));
      if (val('ba-search-user')) params.set('fullName', val('ba-search-user'));
      return run(`/bank-accounts/search?${params.toString()}`);
    }

    case 'delete-bank-account':
      return run(`/bank-accounts/${requireNumber('ba-delete-id', 'Delete Account ID')}`, { method: 'DELETE' });

    case 'record-income':
      return run('/transactions/income', {
        method: 'POST',
        body: JSON.stringify({
          bankAccountId: requireNumber('tx-income-bankAccountId', 'Income Bank Account ID'),
          amount: requireNumber('tx-income-amount', 'Income amount'),
          category: val('tx-income-category'),
          description: val('tx-income-description'),
        }),
      });

    case 'record-expense':
      return run('/transactions/expense', {
        method: 'POST',
        body: JSON.stringify({
          bankAccountId: requireNumber('tx-expense-bankAccountId', 'Expense Bank Account ID'),
          amount: requireNumber('tx-expense-amount', 'Expense amount'),
          category: val('tx-expense-category'),
          description: val('tx-expense-description'),
        }),
      });

    case 'record-atm':
      return run('/transactions/atm-withdrawal', {
        method: 'POST',
        body: JSON.stringify({
          bankAccountId: requireNumber('tx-atm-bankAccountId', 'ATM Bank Account ID'),
          amount: requireNumber('tx-atm-amount', 'ATM amount'),
          description: val('tx-atm-description'),
        }),
      });

    case 'record-bank-expense':
      return run('/transactions/bank-expense', {
        method: 'POST',
        body: JSON.stringify({
          bankAccountId: requireNumber('tx-bankexp-bankAccountId', 'Bank Expense Account ID'),
          amount: requireNumber('tx-bankexp-amount', 'Bank Expense amount'),
          category: val('tx-bankexp-category'),
          description: val('tx-bankexp-description'),
        }),
      });

    case 'list-transactions':
      return run(`/transactions/user/${roleUserId('tx-user-list', 'User ID')}`);

    case 'get-transaction':
      return run(`/transactions/${requireNumber('tx-id', 'Transaction ID')}`);

    case 'transfer-funds': {
      const transferType = requireText('tr-transferType', 'Transfer type');
      const destinationAccountId = num('tr-destinationAccountId');
      const mobileNumber = val('tr-mobileNumber');
      const upiId = val('tr-upiId');
      const selfTransfer = document.getElementById('tr-selfTransfer')?.checked === true;

      return run('/transfers', {
        method: 'POST',
        body: JSON.stringify({
          sourceAccountId: requireNumber('tr-sourceAccountId', 'Source Account ID'),
          destinationAccountId,
          transferType,
          selfTransfer,
          mobileNumber,
          upiId,
          amount: requireNumber('tr-amount', 'Transfer amount'),
          description: val('tr-description'),
        }),
      });
    }

    case 'report-bank-balances':
      return run(`/reports/bank-balances?userId=${reportUserId('r-bank-userId', 'User ID')}`);

    case 'report-monthly-expenses':
      return run(`/reports/monthly-expenses?userId=${reportUserId('r-monthly-userId', 'User ID')}`);

    case 'report-category-expense':
      return run(`/reports/expense-by-category?userId=${reportUserId('r-category-userId', 'User ID')}`);

    case 'report-summary':
      return run(`/reports/income-expense-summary?userId=${reportUserId('r-summary-userId', 'User ID')}`);

    case 'admin-users':
      return run('/admin/users');

    case 'admin-activities':
      return run('/admin/activities');

    default:
      return null;
  }
}

document.addEventListener('click', async event => {
  const target = event.target;
  if (!(target instanceof HTMLElement)) return;
  const action = target.dataset.action;
  if (!action) return;

  target.setAttribute('disabled', 'disabled');
  setInlineStatus(target, 'Processing request...', 'loading');
  try {
    const data = await handleAction(action);
    setInlineStatus(target, `✔ ${getSuccessMessage(action, data)}`, 'success');
  } catch (error) {
    const message = error instanceof Error ? error.message : 'Request failed';
    setInlineStatus(target, `✖ ${message}`, 'error');
    if (!error?.toastShown) {
      toast.error(message);
    }
    console.error(error);
  } finally {
    target.removeAttribute('disabled');
  }
});

const yearEl = document.getElementById('year');
if (yearEl) {
  yearEl.textContent = String(new Date().getFullYear());
}

setStatus('Ready for actions', true);

if (logoutButton) {
  logoutButton.addEventListener('click', () => logout('user-login.html'));
}

bootstrapSession();
