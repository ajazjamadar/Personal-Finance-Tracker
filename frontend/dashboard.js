const TOKEN_KEY = 'fintrack-token';

const token = localStorage.getItem(TOKEN_KEY) || '';
const API_BASE = window.location.protocol.startsWith('http')
  ? `${window.location.protocol}//${window.location.hostname || 'localhost'}:8080/api`
  : 'http://localhost:8080/api';

const statusLine = document.getElementById('statusLine');
const resultEl = document.getElementById('result');

function baseUrl() {
  return API_BASE;
}

function currentToken() {
  return token || '';
}

function setStatus(message, ok = true) {
  if (!statusLine) return;
  statusLine.textContent = message;
  statusLine.className = `status-line ${ok ? 'ok' : 'err'}`;
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

async function api(path, options = {}) {
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
    if (response.status === 401) {
      setStatus('Unauthorized. This action requires an authenticated session.', false);
    } else {
      setStatus(`Failed (${response.status})`, false);
    }
    renderResult(data);
    throw new Error(data?.message || `HTTP ${response.status}`);
  }

  setStatus(`Success (${response.status})`, true);
  renderResult(data);
  return data;
}

async function handleAction(action) {
  switch (action) {
    case 'create-user':
      return api('/users', {
        method: 'POST',
        body: JSON.stringify({
          username: requireText('u-username', 'Username'),
          email: requireText('u-email', 'Email'),
          password: requireText('u-password', 'Password'),
          fullName: requireText('u-fullname', 'Full name'),
        }),
      });

    case 'get-user':
      return api(`/users/${requireNumber('u-id', 'User ID')}`);

    case 'create-bank-account':
      return api('/bank-accounts', {
        method: 'POST',
        body: JSON.stringify({
          userId: requireNumber('ba-userId', 'User ID'),
          bankName: requireText('ba-bankName', 'Bank name'),
          accountNumber: requireText('ba-accountNumber', 'Account number'),
          initialBalance: requireNumber('ba-initialBalance', 'Initial balance'),
        }),
      });

    case 'get-bank-account':
      return api(`/bank-accounts/${requireNumber('ba-id', 'Account ID')}`);

    case 'list-bank-accounts':
      return api(`/bank-accounts/user/${requireNumber('ba-user-list', 'User ID')}`);

    case 'search-bank-accounts': {
      const params = new URLSearchParams();
      if (val('ba-search-bank')) params.set('bankName', val('ba-search-bank'));
      if (val('ba-search-user')) params.set('fullName', val('ba-search-user'));
      return api(`/bank-accounts/search?${params.toString()}`);
    }

    case 'delete-bank-account':
      return api(`/bank-accounts/${requireNumber('ba-delete-id', 'Delete Account ID')}`, { method: 'DELETE' });

    case 'create-wallet':
      return api('/wallets', {
        method: 'POST',
        body: JSON.stringify({
          userId: requireNumber('w-userId', 'User ID'),
          walletName: requireText('w-walletName', 'Wallet name'),
          initialBalance: requireNumber('w-initialBalance', 'Initial balance'),
        }),
      });

    case 'get-wallet':
      return api(`/wallets/${requireNumber('w-id', 'Wallet ID')}`);

    case 'list-wallets':
      return api(`/wallets/user/${requireNumber('w-user-list', 'User ID')}`);

    case 'delete-wallet':
      return api(`/wallets/${requireNumber('w-delete-id', 'Delete Wallet ID')}`, { method: 'DELETE' });

    case 'record-income':
      return api('/transactions/income', {
        method: 'POST',
        body: JSON.stringify({
          walletId: requireNumber('tx-income-walletId', 'Income Wallet ID'),
          amount: requireNumber('tx-income-amount', 'Income amount'),
          category: val('tx-income-category'),
          description: val('tx-income-description'),
        }),
      });

    case 'record-expense':
      return api('/transactions/expense', {
        method: 'POST',
        body: JSON.stringify({
          walletId: requireNumber('tx-expense-walletId', 'Expense Wallet ID'),
          amount: requireNumber('tx-expense-amount', 'Expense amount'),
          category: val('tx-expense-category'),
          description: val('tx-expense-description'),
        }),
      });

    case 'record-atm':
      return api('/transactions/atm-withdrawal', {
        method: 'POST',
        body: JSON.stringify({
          bankAccountId: requireNumber('tx-atm-bankAccountId', 'ATM Bank Account ID'),
          amount: requireNumber('tx-atm-amount', 'ATM amount'),
          description: val('tx-atm-description'),
        }),
      });

    case 'record-bank-expense':
      return api('/transactions/bank-expense', {
        method: 'POST',
        body: JSON.stringify({
          bankAccountId: requireNumber('tx-bankexp-bankAccountId', 'Bank Expense Account ID'),
          amount: requireNumber('tx-bankexp-amount', 'Bank Expense amount'),
          category: val('tx-bankexp-category'),
          description: val('tx-bankexp-description'),
        }),
      });

    case 'list-transactions':
      return api(`/transactions/user/${requireNumber('tx-user-list', 'User ID')}`);

    case 'get-transaction':
      return api(`/transactions/${requireNumber('tx-id', 'Transaction ID')}`);

    case 'transfer-b2w':
      return api('/transfers/bank-to-wallet', {
        method: 'POST',
        body: JSON.stringify({
          sourceId: requireNumber('tr-b2w-sourceId', 'Bank Source ID'),
          destinationId: requireNumber('tr-b2w-destinationId', 'Wallet Destination ID'),
          amount: requireNumber('tr-b2w-amount', 'Transfer amount'),
        }),
      });

    case 'transfer-w2b':
      return api('/transfers/wallet-to-bank', {
        method: 'POST',
        body: JSON.stringify({
          sourceId: requireNumber('tr-w2b-sourceId', 'Wallet Source ID'),
          destinationId: requireNumber('tr-w2b-destinationId', 'Bank Destination ID'),
          amount: requireNumber('tr-w2b-amount', 'Transfer amount'),
        }),
      });

    case 'report-bank-balances':
      return api(`/reports/bank-balances?userId=${requireNumber('r-bank-userId', 'User ID')}`);

    case 'report-monthly-expenses':
      return api(`/reports/monthly-expenses?userId=${requireNumber('r-monthly-userId', 'User ID')}`);

    case 'report-category-expense':
      return api(`/reports/expense-by-category?userId=${requireNumber('r-category-userId', 'User ID')}`);

    case 'report-summary':
      return api(`/reports/income-expense-summary?userId=${requireNumber('r-summary-userId', 'User ID')}`);

    case 'admin-users':
      return api('/admin/users');

    case 'admin-activities':
      return api('/admin/activities');

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
  setStatus(`Executing ${action}...`, true);
  try {
    await handleAction(action);
  } catch (error) {
    setStatus(error instanceof Error ? error.message : 'Request failed', false);
    console.error(error);
  } finally {
    target.removeAttribute('disabled');
  }
});

const yearEl = document.getElementById('year');
if (yearEl) {
  yearEl.textContent = String(new Date().getFullYear());
}

setStatus('Ready', true);
