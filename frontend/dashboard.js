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

// For production on Render, use the same origin
// For local development, defaults to localhost:8080
const API_BASE = (() => {
  if (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1') {
    return 'http://localhost:8080/api'; // Local development
  }
  return `${window.location.protocol}//${window.location.host}/api`; // Production (same origin)
})();

const statusLineEls = Array.from(document.querySelectorAll('#statusLine'));
const resultEls = Array.from(document.querySelectorAll('#result'));
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
const usersLeadEl = document.getElementById('usersLead');
const accountsLeadEl = document.getElementById('accountsLead');
const usersSelfWorkspaceEl = document.getElementById('usersSelfWorkspace');
const usersAdminWorkspaceEl = document.getElementById('usersAdminWorkspace');
const accountsSelfWorkspaceEl = document.getElementById('accountsSelfWorkspace');
const accountsAdminWorkspaceEl = document.getElementById('accountsAdminWorkspace');
const adminDashboardStatusEl = document.getElementById('adminDashboardStatus');
const adminDashboardUpdatedAtEl = document.getElementById('adminDashboardUpdatedAt');
const adminMetricGridEl = document.getElementById('adminMetricGrid');
const adminSnapshotGridEl = document.getElementById('adminSnapshotGrid');
const adminHealthGridEl = document.getElementById('adminHealthGrid');
const adminChartsGridEl = document.getElementById('adminChartsGrid');
const adminRecentTransactionsEl = document.getElementById('adminRecentTransactions');
const adminRecentUsersEl = document.getElementById('adminRecentUsers');
const adminRecentTransfersEl = document.getElementById('adminRecentTransfers');
const adminRecentWalletTransactionsEl = document.getElementById('adminRecentWalletTransactions');
const adminFailedTransfersEl = document.getElementById('adminFailedTransfers');
const adminLowBalancesEl = document.getElementById('adminLowBalances');
const adminLowWalletBalancesEl = document.getElementById('adminLowWalletBalances');
const adminSuspiciousTransactionsEl = document.getElementById('adminSuspiciousTransactions');
const adminSuspiciousWalletTransactionsEl = document.getElementById('adminSuspiciousWalletTransactions');

function activeWorkspaceElement(elements) {
  return elements.find(node => !node.closest('[hidden]')) || elements[0] || null;
}

function currentStatusLine() {
  return activeWorkspaceElement(statusLineEls);
}

function currentResultEl() {
  return activeWorkspaceElement(resultEls);
}

const actionFeedback = {
  'admin-dashboard': { success: 'Admin dashboard refreshed', error: 'Failed to load admin dashboard' },
  'update-user': { success: 'User profile updated successfully', error: 'Failed to update profile' },
  'create-bank-account': { success: 'Bank account created successfully', error: 'Failed to create bank account' },
  'get-bank-account': { success: 'Bank account loaded successfully', error: 'Failed to fetch bank account' },
  'list-bank-accounts': { success: 'Bank accounts loaded successfully', error: 'Failed to list bank accounts' },
  'delete-bank-account': { success: 'Bank account deleted successfully', error: 'Failed to delete bank account' },
  'record-income': { success: 'Income recorded successfully', error: 'Failed to record income' },
  'record-expense': { success: 'Expense recorded successfully', error: 'Failed to record expense' },
  'list-transactions': { success: 'Transactions loaded', error: 'Failed to list transactions' },
  'clear-tx-filters': { success: 'Transaction filters cleared', error: 'Failed to clear transaction filters' },
  'get-transaction': { success: 'Transaction loaded', error: 'Failed to fetch transaction' },
  'transfer-funds': { success: 'Transfer completed successfully', error: 'Failed to transfer funds' },
  'report-bank-balances': { success: 'Bank balance report generated', error: 'Failed to generate bank balance report' },
  'report-monthly-expenses': { success: 'Monthly expense report generated', error: 'Failed to generate monthly expense report' },
  'report-category-expense': { success: 'Category report generated', error: 'Failed to generate category report' },
  'report-summary': { success: 'Income/expense summary generated', error: 'Failed to generate income/expense summary' },
  'report-overview': { success: 'Financial overview generated', error: 'Failed to generate financial overview' },
  'report-bank-statement-csv': { success: 'Bank statement CSV downloaded', error: 'Failed to download bank statement CSV' },
  'report-bank-statement-pdf': { success: 'Bank statement PDF downloaded', error: 'Failed to download bank statement PDF' },
  'report-overview-csv': { success: 'Overall report CSV downloaded', error: 'Failed to download overall report CSV' },
  'report-overview-pdf': { success: 'Overall report PDF downloaded', error: 'Failed to download overall report PDF' },
  'create-wallet': { success: 'Wallet created successfully', error: 'Failed to create wallet' },
  'list-wallets': { success: 'Wallets loaded successfully', error: 'Failed to list wallets' },
  'get-wallet': { success: 'Wallet loaded successfully', error: 'Failed to load wallet' },
  'delete-wallet': { success: 'Wallet deleted successfully', error: 'Failed to delete wallet' },
  'wallet-deposit': { success: 'Deposit completed successfully', error: 'Failed to deposit wallet amount' },
  'wallet-withdraw': { success: 'Withdrawal completed successfully', error: 'Failed to withdraw wallet amount' },
  'wallet-transfer': { success: 'Wallet transfer completed successfully', error: 'Failed to transfer between wallets' },
  'wallet-list-transactions': { success: 'Wallet transactions loaded successfully', error: 'Failed to load wallet transactions' },
  'admin-users': { success: 'Admin users loaded', error: 'Failed to load admin users' },
  'admin-create-user': { success: 'User profile created successfully', error: 'Failed to create user profile' },
  'admin-update-user': { success: 'User profile updated successfully', error: 'Failed to update user profile' },
  'admin-accounts': { success: 'Admin accounts loaded', error: 'Failed to load admin accounts' },
  'admin-update-account': { success: 'User bank account updated successfully', error: 'Failed to update user bank account' },
  'admin-monthly-performance-csv': { success: 'Monthly performance CSV downloaded', error: 'Failed to download monthly performance CSV' },
  'admin-monthly-performance-pdf': { success: 'Monthly performance PDF downloaded', error: 'Failed to download monthly performance PDF' },
  'admin-system-report-csv': { success: 'System report CSV downloaded', error: 'Failed to download system report CSV' },
  'admin-system-report-pdf': { success: 'System report PDF downloaded', error: 'Failed to download system report PDF' },
  'admin-transaction-report-csv': { success: 'Transaction report CSV downloaded', error: 'Failed to download transaction report CSV' },
  'admin-transaction-report-pdf': { success: 'Transaction report PDF downloaded', error: 'Failed to download transaction report PDF' },
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

function workspaceLoginRoute() {
  return document.body.dataset.adminWorkspace ? 'admin-login.html' : 'user-login.html';
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

function reportUserId() {
  const uid = currentUserId();
  if (!uid) {
    throw new Error('Unable to resolve current user session. Please login again.');
  }
  return uid;
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
  const adminWorkspace = document.body.dataset.adminWorkspace;
  if (adminWorkspace && !isAdmin()) {
    window.location.href = 'admin-login.html';
    return;
  }

  if (adminWorkspace && isAdmin()) {
    return;
  }

  if (isAdmin()) {
    const path = window.location.pathname;
    if (path.endsWith('/users.html')) {
      window.location.href = 'admin-users.html';
      return;
    }
    if (path.endsWith('/accounts.html')) {
      window.location.href = 'admin-accounts.html';
      return;
    }
    if (
      path.endsWith('/reports.html') ||
      path.endsWith('/transactions.html') ||
      path.endsWith('/transfers.html') ||
      path.endsWith('/wallets.html') ||
      path.endsWith('/wallet-transactions.html')
    ) {
      window.location.href = 'admin-dashboard.html';
      return;
    }

    document.querySelectorAll('.user-only-nav').forEach(link => {
      link.hidden = true;
    });

    if (usersSelfWorkspaceEl) usersSelfWorkspaceEl.hidden = true;
    if (usersAdminWorkspaceEl) usersAdminWorkspaceEl.hidden = false;
    if (accountsSelfWorkspaceEl) accountsSelfWorkspaceEl.hidden = true;
    if (accountsAdminWorkspaceEl) accountsAdminWorkspaceEl.hidden = false;
    if (usersLeadEl) {
      usersLeadEl.textContent = 'Create, update, and view user profiles in the admin workspace.';
    }
    if (accountsLeadEl) {
      accountsLeadEl.textContent = 'View all accounts and update user bank accounts in the admin workspace.';
    }

    return;
  }

  document.querySelectorAll('.user-only-nav').forEach(link => {
    link.hidden = false;
  });

  if (usersSelfWorkspaceEl) usersSelfWorkspaceEl.hidden = false;
  if (usersAdminWorkspaceEl) usersAdminWorkspaceEl.hidden = true;
  if (accountsSelfWorkspaceEl) accountsSelfWorkspaceEl.hidden = false;
  if (accountsAdminWorkspaceEl) accountsAdminWorkspaceEl.hidden = true;

  if (usersLeadEl) {
    usersLeadEl.textContent = 'Your profile details are fetched automatically after login.';
  }
  if (accountsLeadEl) {
    accountsLeadEl.textContent = 'Create, fetch, list, and delete bank accounts.';
  }

  ['admin-users', 'admin-create-user', 'admin-update-user', 'admin-accounts', 'admin-update-account'].forEach(action => {
    const button = document.querySelector(`[data-action="${action}"]`);
    if (!button) return;
    button.setAttribute('disabled', 'disabled');
    button.setAttribute('title', 'Admin access required');
  });
}

async function bootstrapSession() {
  if (!currentToken()) {
    logout(workspaceLoginRoute());
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
    if (document.body.dataset.adminWorkspace === 'dashboard') {
      try {
        await loadAdminDashboard(false);
      } catch (error) {
        console.error(error);
      }
    }
  } catch {
    logout(workspaceLoginRoute());
  }
}

function setStatus(message, ok = true) {
  const statusLine = currentStatusLine();
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

function optionalText(id) {
  const value = val(id);
  return value || null;
}

function clearFieldValues(ids = []) {
  ids.forEach(id => {
    const field = document.getElementById(id);
    if (!(field instanceof HTMLInputElement || field instanceof HTMLSelectElement || field instanceof HTMLTextAreaElement)) {
      return;
    }
    field.value = '';
  });
}

function escapeHtml(input) {
  return String(input)
    .replaceAll('&', '&amp;')
    .replaceAll('<', '&lt;')
    .replaceAll('>', '&gt;')
    .replaceAll('"', '&quot;')
    .replaceAll("'", '&#39;');
}

function humanizeKey(key) {
  if (!key) return '';
  return String(key)
    .replaceAll('_', ' ')
    .replace(/([a-z0-9])([A-Z])/g, '$1 $2')
    .replace(/\s+/g, ' ')
    .trim()
    .replace(/\b\w/g, char => char.toUpperCase());
}

function renderWorkspaceLayout({ title, subtitle, metrics = [], bodyHtml = '', highlights = [] }) {
  const metricCards = metrics.filter(item => item && item.value != null && item.value !== '').map(item => `
    <article class="workspace-metric ${item.tone || 'neutral'}">
      <p>${escapeHtml(item.label || 'Metric')}</p>
      <strong>${escapeHtml(String(item.value))}</strong>
    </article>
  `).join('');

  const highlightTags = highlights.filter(Boolean).map(tag => `
    <span class="workspace-tag">${escapeHtml(String(tag))}</span>
  `).join('');

  return `
    <section class="workspace-result">
      <header class="workspace-result-head">
        <div>
          <p class="workspace-result-kicker">${escapeHtml(title || 'Result')}</p>
          <h4>${escapeHtml(subtitle || 'Processed response')}</h4>
        </div>
      </header>
      ${metricCards ? `<div class="workspace-metrics">${metricCards}</div>` : ''}
      ${highlightTags ? `<div class="workspace-tags">${highlightTags}</div>` : ''}
      <div class="workspace-body">${bodyHtml}</div>
    </section>
  `;
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
        <thead><tr>${columns.map(c => `<th>${escapeHtml(humanizeKey(c))}</th>`).join('')}</tr></thead>
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
        return `<div class="kv-item"><div class="kv-key">${escapeHtml(humanizeKey(key))}</div><div class="kv-val">${escapeHtml(printable)}</div></div>`;
      }).join('')}
    </div>
  `;
}

function isBankAccountRecord(item) {
  if (!item || typeof item !== 'object') return false;
  const normalized = normalizeBankAccountRecord(item);
  return normalized !== null;
}

function normalizeBankAccountRecord(item) {
  if (!item || typeof item !== 'object') return null;

  const normalized = {
    id: item.id ?? item.bankAccountId ?? null,
    userId: item.userId ?? item.user?.id ?? item.user_id ?? null,
    bankName: item.bankName ?? item.bank?.bankName ?? item.bank_name ?? null,
    accountNumber: item.accountNumber ?? item.account_no ?? item.account_number ?? null,
    balance: item.balance ?? null,
    createdAt: item.createdAt ?? item.accountCreatedAt ?? item.created_at ?? null,
  };

  const required = ['id', 'userId', 'bankName', 'accountNumber', 'balance'];
  const hasRequiredFields = required.every(key => normalized[key] !== null && normalized[key] !== undefined);
  return hasRequiredFields ? normalized : null;
}

function extractBankAccountRecords(data) {
  if (Array.isArray(data)) {
    return data.map(normalizeBankAccountRecord).filter(Boolean);
  }

  if (data && typeof data === 'object') {
    const possibleLists = [data.accounts, data.data, data.items, data.content];
    for (const list of possibleLists) {
      if (Array.isArray(list)) {
        return list.map(normalizeBankAccountRecord).filter(Boolean);
      }
    }
  }

  return [];
}

function normalizeWalletRecord(item) {
  if (!item || typeof item !== 'object') return null;

  const normalized = {
    id: item.id ?? null,
    userId: item.userId ?? item.user?.id ?? null,
    name: item.name ?? null,
    balance: item.balance ?? null,
    currency: item.currency ?? 'INR',
    createdAt: item.createdAt ?? null,
    updatedAt: item.updatedAt ?? null,
  };

  const hasRequiredFields = normalized.id !== null
    && normalized.name !== null
    && normalized.balance !== null;
  return hasRequiredFields ? normalized : null;
}

function extractWalletRecords(data) {
  if (Array.isArray(data)) {
    return data.map(normalizeWalletRecord).filter(Boolean);
  }
  if (data && typeof data === 'object') {
    const single = normalizeWalletRecord(data);
    return single ? [single] : [];
  }
  return [];
}

function renderWalletCards(records) {
  const normalizedRecords = records.map(normalizeWalletRecord).filter(Boolean);

  if (!normalizedRecords.length) {
    return '<p class="result-empty">No wallets found.</p>';
  }

  return `
    <div class="account-result-cards">
      ${normalizedRecords.map(wallet => `
        <article class="account-result-card">
          <p><strong>Wallet ID:</strong> ${escapeHtml(wallet.id ?? '-')}</p>
          <p><strong>Name:</strong> ${escapeHtml(wallet.name ?? '-')}</p>
          <p><strong>Balance:</strong> ${escapeHtml(formatCurrency(wallet.balance))}</p>
          <p><strong>Currency:</strong> ${escapeHtml(wallet.currency ?? 'INR')}</p>
          <p><strong>Created At:</strong> ${escapeHtml(formatDateTime(wallet.createdAt))}</p>
          <p><strong>Updated At:</strong> ${escapeHtml(formatDateTime(wallet.updatedAt))}</p>
        </article>
      `).join('')}
    </div>
  `;
}

function formatDateTime(value) {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return String(value);
  return date.toLocaleString();
}

function formatNumber(value) {
  const numericValue = Number(value ?? 0);
  if (Number.isNaN(numericValue)) return String(value ?? '-');
  return new Intl.NumberFormat('en-IN', { maximumFractionDigits: 0 }).format(numericValue);
}

function formatCurrency(value) {
  const numericValue = Number(value ?? 0);
  if (Number.isNaN(numericValue)) return String(value ?? '-');
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 2,
  }).format(numericValue);
}

function dashboardEmpty(message) {
  return `<p class="dashboard-empty">${escapeHtml(message)}</p>`;
}

function dashboardStatusClass(type = 'info') {
  if (type === 'success') return 'success';
  if (type === 'error') return 'error';
  if (type === 'loading') return 'loading';
  return 'info';
}

function setAdminDashboardStatus(message, type = 'info') {
  if (!adminDashboardStatusEl) return;
  adminDashboardStatusEl.textContent = message;
  adminDashboardStatusEl.className = `dashboard-status ${dashboardStatusClass(type)}`;
}

function adminStatusTone(status) {
  const normalized = String(status || '').toUpperCase();
  if (normalized === 'FAILED') return 'danger';
  if (normalized === 'PENDING' || normalized === 'INITIATED') return 'warning';
  if (normalized === 'DEBIT') return 'warning';
  if (normalized === 'SENT' || normalized === 'SUCCESS' || normalized === 'UP') return 'success';
  if (normalized === 'CREDIT') return 'success';
  return 'neutral';
}

function renderStatusPill(status) {
  return `<span class="status-pill ${adminStatusTone(status)}">${escapeHtml(status || 'UNKNOWN')}</span>`;
}

function renderAdminMetricGrid(metrics) {
  if (!adminMetricGridEl || !metrics) return;

  const totalBankAccounts = Number(metrics.totalBankAccounts ?? metrics.totalAccounts ?? 0);
  const totalWallets = Number(metrics.totalWallets ?? 0);
  const totalAccounts = Number(metrics.totalAccounts ?? (totalBankAccounts + totalWallets));

  const cards = [
    { label: 'Total Users', value: formatNumber(metrics.totalUsers), tone: 'users' },
    { label: 'Total Bank Accounts', value: formatNumber(totalBankAccounts), tone: 'accounts' },
    { label: 'Total Wallets', value: formatNumber(totalWallets), tone: 'wallets' },
    { label: 'Total Accounts (Bank + Wallet)', value: formatNumber(totalAccounts), tone: 'platform' },
    { label: 'Total Transactions', value: formatNumber(metrics.totalTransactions), tone: 'transactions' },
    { label: 'Total Wallet Transactions', value: formatNumber(metrics.totalWalletTransactions), tone: 'ledger' },
    { label: 'Total Transfer Volume', value: formatCurrency(metrics.totalTransferVolume), tone: 'volume' },
  ];

  adminMetricGridEl.innerHTML = cards.map(card => `
    <article class="metric-card ${card.tone}">
      <p class="metric-label">${escapeHtml(card.label)}</p>
      <h3 class="metric-value">${escapeHtml(card.value)}</h3>
    </article>
  `).join('');
}

function renderSnapshotWindow(title, window) {
  if (!window) {
    return dashboardEmpty(`No ${title.toLowerCase()} snapshot available.`);
  }

  const stats = [
    { label: 'Transactions', value: formatNumber(window.transactions) },
    { label: 'Wallet Txns', value: formatNumber(window.walletTransactions) },
    { label: 'Transfers', value: formatNumber(window.transfers) },
    { label: 'New Users', value: formatNumber(window.newUsers) },
  ];

  return `
    <article class="snapshot-card">
      <div class="snapshot-head">
        <h4>${escapeHtml(title)}</h4>
      </div>
      <div class="snapshot-stats">
        ${stats.map(stat => `
          <div class="snapshot-stat">
            <span>${escapeHtml(stat.label)}</span>
            <strong>${escapeHtml(stat.value)}</strong>
          </div>
        `).join('')}
      </div>
    </article>
  `;
}

function renderAdminSnapshot(snapshot) {
  if (!adminSnapshotGridEl) return;
  adminSnapshotGridEl.innerHTML = `
    ${renderSnapshotWindow('Today', snapshot?.today)}
    ${renderSnapshotWindow('This Month', snapshot?.thisMonth)}
  `;
}

function renderAdminHealth(health) {
  if (!adminHealthGridEl || !health) return;

  const items = [
    { label: 'Failed Transactions', value: formatNumber(health.failedTransactionsCount), tone: health.failedTransactionsCount > 0 ? 'danger' : 'success' },
    { label: 'Pending Transfers', value: formatNumber(health.pendingTransfersCount), tone: health.pendingTransfersCount > 0 ? 'warning' : 'success' },
    { label: 'Failed Transfers', value: formatNumber(health.failedTransfersCount), tone: health.failedTransfersCount > 0 ? 'danger' : 'success' },
    { label: 'Low Wallet Balances', value: formatNumber(health.lowWalletBalanceCount), tone: health.lowWalletBalanceCount > 0 ? 'warning' : 'success' },
    { label: 'API / System', value: health.apiStatus || 'UNKNOWN', tone: adminStatusTone(health.apiStatus) },
  ];

  adminHealthGridEl.innerHTML = items.map(item => `
    <article class="health-card ${item.tone}">
      <p>${escapeHtml(item.label)}</p>
      <div class="health-value">${item.label === 'API / System' ? renderStatusPill(item.value) : `<strong>${escapeHtml(item.value)}</strong>`}</div>
    </article>
  `).join('');
}

function renderAdminChartLegend(series, formatter = formatNumber) {
  const total = series.reduce((sum, item) => sum + amountOf(item.value), 0);
  return `
    <div class="admin-chart-legend">
      ${series.map((item, index) => {
        const value = amountOf(item.value);
        const percent = total > 0 ? (value / total) * 100 : 0;
        return `
          <div class="admin-chart-legend-item">
            <span class="swatch" style="--swatch:${reportPalette(index)};"></span>
            <span class="label">${escapeHtml(item.label)}</span>
            <span class="value">${escapeHtml(formatter(value))}</span>
            <span class="percent">${escapeHtml(percent.toFixed(1))}%</span>
          </div>
        `;
      }).join('')}
    </div>
  `;
}

function renderAdminDonutCard({ title, subtitle, series, formatter = formatNumber }) {
  const normalized = (Array.isArray(series) ? series : [])
    .map(item => ({ label: item.label || 'Item', value: amountOf(item.value) }))
    .filter(item => item.value > 0);

  if (!normalized.length) {
    return `
      <article class="admin-chart-card">
        <h4>${escapeHtml(title)}</h4>
        <p class="admin-chart-subtitle">${escapeHtml(subtitle)}</p>
        ${dashboardEmpty('No activity data available for this chart.')}
      </article>
    `;
  }

  const total = normalized.reduce((sum, item) => sum + item.value, 0);

  return `
    <article class="admin-chart-card">
      <h4>${escapeHtml(title)}</h4>
      <p class="admin-chart-subtitle">${escapeHtml(subtitle)}</p>
      <div class="admin-chart-body">
        <div class="admin-chart-circle admin-chart-donut" style="background: conic-gradient(${buildConicGradient(normalized)});">
          <div class="admin-chart-donut-center">
            <span>Total</span>
            <strong>${escapeHtml(formatter(total))}</strong>
          </div>
        </div>
        ${renderAdminChartLegend(normalized, formatter)}
      </div>
    </article>
  `;
}

function renderAdminComparisonCard({ title, subtitle, rows }) {
  const normalizedRows = (Array.isArray(rows) ? rows : [])
    .map(item => ({
      label: item.label || 'Metric',
      today: amountOf(item.today),
      month: amountOf(item.month),
    }));

  if (!normalizedRows.length) {
    return `
      <article class="admin-chart-card">
        <h4>${escapeHtml(title)}</h4>
        <p class="admin-chart-subtitle">${escapeHtml(subtitle)}</p>
        ${dashboardEmpty('No comparison data available.')}
      </article>
    `;
  }

  const maxValue = Math.max(
    ...normalizedRows.flatMap(item => [item.today, item.month]),
    1,
  );

  return `
    <article class="admin-chart-card">
      <h4>${escapeHtml(title)}</h4>
      <p class="admin-chart-subtitle">${escapeHtml(subtitle)}</p>
      <div class="admin-compare-chart">
        ${normalizedRows.map(item => {
          const todayWidth = ((item.today / maxValue) * 100).toFixed(2);
          const monthWidth = ((item.month / maxValue) * 100).toFixed(2);
          return `
            <div class="admin-compare-row">
              <p>${escapeHtml(item.label)}</p>
              <div class="admin-compare-bars">
                <div class="admin-compare-track">
                  <div class="admin-compare-fill today" style="width:${todayWidth}%;"></div>
                </div>
                <span>${escapeHtml(formatNumber(item.today))}</span>
                <div class="admin-compare-track">
                  <div class="admin-compare-fill month" style="width:${monthWidth}%;"></div>
                </div>
                <span>${escapeHtml(formatNumber(item.month))}</span>
              </div>
            </div>
          `;
        }).join('')}
      </div>
      <p class="admin-compare-key">Today vs This Month</p>
    </article>
  `;
}

function renderAdminCharts(data) {
  if (!adminChartsGridEl || !data) return;

  const keyMetrics = data.keyMetrics || {};
  const today = data.snapshot?.today || {};
  const thisMonth = data.snapshot?.thisMonth || {};
  const alerts = data.alerts || {};

  const totalBankAccounts = amountOf(keyMetrics.totalBankAccounts ?? keyMetrics.totalAccounts ?? 0);
  const totalWallets = amountOf(keyMetrics.totalWallets ?? 0);

  const monthActivitySeries = [
    { label: 'Transactions', value: thisMonth.transactions },
    { label: 'Wallet Txns', value: thisMonth.walletTransactions },
    { label: 'Transfers', value: thisMonth.transfers },
    { label: 'New Users', value: thisMonth.newUsers },
  ];

  const alertSeries = [
    { label: 'Failed Transfers', value: Array.isArray(alerts.failedTransfers) ? alerts.failedTransfers.length : 0 },
    { label: 'Low Accounts', value: Array.isArray(alerts.lowBalanceIssues) ? alerts.lowBalanceIssues.length : 0 },
    { label: 'Low Wallets', value: Array.isArray(alerts.lowWalletBalanceIssues) ? alerts.lowWalletBalanceIssues.length : 0 },
    { label: 'Suspicious Txns', value: Array.isArray(alerts.suspiciousTransactions) ? alerts.suspiciousTransactions.length : 0 },
    { label: 'Suspicious Wallet Txns', value: Array.isArray(alerts.suspiciousWalletTransactions) ? alerts.suspiciousWalletTransactions.length : 0 },
  ];

  const comparisonRows = [
    { label: 'Transactions', today: today.transactions, month: thisMonth.transactions },
    { label: 'Wallet Transactions', today: today.walletTransactions, month: thisMonth.walletTransactions },
    { label: 'Transfers', today: today.transfers, month: thisMonth.transfers },
    { label: 'New Users', today: today.newUsers, month: thisMonth.newUsers },
  ];

  adminChartsGridEl.innerHTML = [
    renderAdminDonutCard({
      title: 'Account Distribution',
      subtitle: 'Bank vs wallet account footprint on the platform',
      series: [
        { label: 'Bank Accounts', value: totalBankAccounts },
        { label: 'Wallets', value: totalWallets },
      ],
    }),
    renderAdminDonutCard({
      title: 'Monthly Activity Mix',
      subtitle: 'Current month operations across all user activity channels',
      series: monthActivitySeries,
    }),
    renderAdminDonutCard({
      title: 'Alert Mix',
      subtitle: 'Distribution of active watchlist alerts',
      series: alertSeries,
    }),
    renderAdminComparisonCard({
      title: 'User Activity Trend',
      subtitle: 'Today compared with this month totals',
      rows: comparisonRows,
    }),
  ].join('');
}

function renderDashboardTable(columns, records, emptyMessage) {
  if (!Array.isArray(records) || !records.length) {
    return dashboardEmpty(emptyMessage);
  }

  return `
    <div class="dashboard-table-wrap">
      <table class="dashboard-table">
        <thead>
          <tr>${columns.map(column => `<th>${escapeHtml(column.label)}</th>`).join('')}</tr>
        </thead>
        <tbody>
          ${records.map(record => `
            <tr>
              ${columns.map(column => {
                const rawValue = typeof column.value === 'function' ? column.value(record) : record?.[column.value];
                const html = column.html === true ? rawValue : escapeHtml(rawValue ?? '-');
                return `<td>${html}</td>`;
              }).join('')}
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `;
}

function renderRecentTransactions(records) {
  if (!adminRecentTransactionsEl) return;
  adminRecentTransactionsEl.innerHTML = renderDashboardTable([
    { label: 'ID', value: item => item.id ?? '-' },
    { label: 'User', value: item => item.userName || '-' },
    { label: 'Type', value: item => item.transactionType || '-' },
    { label: 'Status', value: item => renderStatusPill(item.status), html: true },
    { label: 'Amount', value: item => formatCurrency(item.amount) },
    { label: 'When', value: item => formatDateTime(item.createdAt) },
  ], records, 'No recent transactions available.');
}

function renderRecentUsers(records) {
  if (!adminRecentUsersEl) return;
  adminRecentUsersEl.innerHTML = renderDashboardTable([
    { label: 'User', value: item => item.fullName || '-' },
    { label: 'Email', value: item => item.email || '-' },
    { label: 'Role', value: item => renderStatusPill(item.role), html: true },
    { label: 'Joined', value: item => formatDateTime(item.createdAt) },
  ], records, 'No recent registrations found.');
}

function renderRecentTransfers(records) {
  if (!adminRecentTransfersEl) return;
  adminRecentTransfersEl.innerHTML = renderDashboardTable([
    { label: 'ID', value: item => item.id ?? '-' },
    { label: 'User', value: item => item.userName || '-' },
    { label: 'Route', value: item => item.transferType || '-' },
    { label: 'Status', value: item => renderStatusPill(item.status), html: true },
    { label: 'Amount', value: item => formatCurrency(item.amount) },
    { label: 'Destination', value: item => item.receiverName || item.destinationValue || '-' },
  ], records, 'No recent transfers found.');
}

function renderRecentWalletTransactions(records) {
  if (!adminRecentWalletTransactionsEl) return;
  adminRecentWalletTransactionsEl.innerHTML = renderDashboardTable([
    { label: 'ID', value: item => item.id ?? '-' },
    { label: 'Wallet', value: item => item.walletName || `#${item.walletId ?? '-'}` },
    { label: 'User', value: item => item.userName || '-' },
    { label: 'Type', value: item => renderStatusPill(item.type), html: true },
    { label: 'Amount', value: item => formatCurrency(item.amount) },
    { label: 'When', value: item => formatDateTime(item.createdAt) },
  ], records, 'No recent wallet transactions found.');
}

function renderAlertList(targetEl, items, renderItem, emptyMessage) {
  if (!targetEl) return;
  if (!Array.isArray(items) || !items.length) {
    targetEl.innerHTML = dashboardEmpty(emptyMessage);
    return;
  }
  targetEl.innerHTML = items.map(renderItem).join('');
}

function renderAdminAlerts(alerts, thresholds) {
  renderAlertList(
    adminFailedTransfersEl,
    alerts?.failedTransfers,
    item => `
      <article class="alert-item danger">
        <div class="alert-item-head">
          <strong>Transfer #${escapeHtml(item.id ?? '-')}</strong>
          ${renderStatusPill(item.status)}
        </div>
        <p>${escapeHtml(item.userName || 'Unknown user')} attempted ${escapeHtml(item.transferType || 'TRANSFER')} for ${escapeHtml(formatCurrency(item.amount))}.</p>
        <p class="alert-meta">${escapeHtml(item.receiverName || item.destinationValue || 'Unknown destination')} • ${escapeHtml(formatDateTime(item.createdAt))}</p>
      </article>
    `,
    'No failed transfers right now.'
  );

  renderAlertList(
    adminLowBalancesEl,
    alerts?.lowBalanceIssues,
    item => `
      <article class="alert-item warning">
        <div class="alert-item-head">
          <strong>${escapeHtml(item.bankName || 'Bank Account')}</strong>
          <span class="alert-balance">${escapeHtml(formatCurrency(item.balance))}</span>
        </div>
        <p>${escapeHtml(item.userName || 'Unknown user')} • Account ${escapeHtml(item.accountNumber || '-')}</p>
        <p class="alert-meta">Threshold ${escapeHtml(formatCurrency(thresholds?.lowBalanceThreshold))} • Opened ${escapeHtml(formatDateTime(item.createdAt))}</p>
      </article>
    `,
    'No low balance accounts below the alert threshold.'
  );

  renderAlertList(
    adminLowWalletBalancesEl,
    alerts?.lowWalletBalanceIssues,
    item => `
      <article class="alert-item warning">
        <div class="alert-item-head">
          <strong>${escapeHtml(item.walletName || `Wallet #${item.walletId ?? '-'}`)}</strong>
          <span class="alert-balance">${escapeHtml(formatCurrency(item.balance))}</span>
        </div>
        <p>${escapeHtml(item.userName || 'Unknown user')} • Wallet #${escapeHtml(item.walletId ?? '-')}</p>
        <p class="alert-meta">Threshold ${escapeHtml(formatCurrency(thresholds?.lowWalletBalanceThreshold))} • Opened ${escapeHtml(formatDateTime(item.createdAt))}</p>
      </article>
    `,
    'No low wallet balances below the alert threshold.'
  );

  renderAlertList(
    adminSuspiciousTransactionsEl,
    alerts?.suspiciousTransactions,
    item => `
      <article class="alert-item neutral">
        <div class="alert-item-head">
          <strong>${escapeHtml(item.transactionType || 'Transaction')} #${escapeHtml(item.id ?? '-')}</strong>
          ${renderStatusPill(item.status)}
        </div>
        <p>${escapeHtml(item.userName || 'Unknown user')} moved ${escapeHtml(formatCurrency(item.amount))}.</p>
        <p class="alert-meta">${escapeHtml(item.receiverName || item.description || 'High-value transaction')} • ${escapeHtml(formatDateTime(item.createdAt))}</p>
      </article>
    `,
    'No suspicious high-value transactions detected.'
  );

  renderAlertList(
    adminSuspiciousWalletTransactionsEl,
    alerts?.suspiciousWalletTransactions,
    item => `
      <article class="alert-item neutral">
        <div class="alert-item-head">
          <strong>${escapeHtml(item.walletName || `Wallet #${item.walletId ?? '-'}`)} • #${escapeHtml(item.id ?? '-')}</strong>
          ${renderStatusPill(item.type)}
        </div>
        <p>${escapeHtml(item.userName || 'Unknown user')} moved ${escapeHtml(formatCurrency(item.amount))}.</p>
        <p class="alert-meta">Threshold ${escapeHtml(formatCurrency(thresholds?.suspiciousWalletTransactionThreshold))} • ${escapeHtml(formatDateTime(item.createdAt))}</p>
      </article>
    `,
    'No suspicious high-value wallet activity detected.'
  );
}

function hasAdminDashboardShell() {
  return Boolean(adminMetricGridEl || adminSnapshotGridEl || adminHealthGridEl || adminChartsGridEl);
}

function renderAdminDashboard(data) {
  if (!hasAdminDashboardShell() || !data) return;

  renderAdminMetricGrid(data.keyMetrics);
  renderAdminSnapshot(data.snapshot);
  renderAdminHealth(data.systemHealth);
  renderAdminCharts(data);
  renderRecentTransactions(data.recentActivity?.latestTransactions);
  renderRecentWalletTransactions(data.recentActivity?.latestWalletTransactions);
  renderRecentUsers(data.recentActivity?.recentUserRegistrations);
  renderRecentTransfers(data.recentActivity?.recentTransfers);
  renderAdminAlerts(data.alerts, data.thresholds);

  if (adminDashboardUpdatedAtEl) {
    adminDashboardUpdatedAtEl.textContent = `Last updated ${formatDateTime(data.generatedAt)}`;
  }
}

function normalizeTransactionRecord(item) {
  if (!item || typeof item !== 'object') return null;
  if (!('transactionType' in item) || !('amount' in item)) return null;

  return {
    id: item.id ?? '-',
    userId: item.userId ?? '-',
    transactionType: item.transactionType ?? '-',
    transferType: item.transferType ?? '-',
    selfTransfer: item.selfTransfer ?? '-',
    sourceAccountId: item.sourceAccountId ?? '-',
    destinationAccountId: item.destinationAccountId ?? '-',
    destinationValue: item.destinationValue ?? '-',
    amount: item.amount ?? '-',
    category: item.category ?? '-',
    status: item.status ?? '-',
    paymentMethod: item.paymentMethod ?? '-',
    receiverName: item.receiverName ?? '-',
    description: item.description ?? '-',
    createdAtRaw: item.createdAt ?? null,
    createdAt: formatDateTime(item.createdAt),
  };
}

function extractTransactionRecords(data) {
  if (Array.isArray(data)) {
    return data.map(normalizeTransactionRecord).filter(Boolean);
  }

  if (data && typeof data === 'object') {
    const single = normalizeTransactionRecord(data);
    if (single) return [single];

    if (Array.isArray(data.transactions)) {
      return data.transactions.map(normalizeTransactionRecord).filter(Boolean);
    }
  }

  return [];
}

function amountOfTransaction(record) {
  const value = Number(record?.amount ?? 0);
  return Number.isFinite(value) ? value : 0;
}

function transactionTypeTone(type) {
  const normalized = String(type || '').toUpperCase();
  if (normalized === 'CREDIT') return 'success';
  if (normalized === 'DEBIT') return 'warning';
  return 'neutral';
}

function renderTransactionTypePill(type) {
  return `<span class="status-pill ${transactionTypeTone(type)}">${escapeHtml(type || 'UNKNOWN')}</span>`;
}

function summarizeTransactions(records) {
  const summary = {
    totalCount: records.length,
    creditCount: 0,
    debitCount: 0,
    creditAmount: 0,
    debitAmount: 0,
    latestAt: null,
  };

  records.forEach(record => {
    const type = String(record.transactionType || '').toUpperCase();
    const amount = amountOfTransaction(record);
    if (type === 'CREDIT') {
      summary.creditCount += 1;
      summary.creditAmount += amount;
    } else if (type === 'DEBIT') {
      summary.debitCount += 1;
      summary.debitAmount += amount;
    }

    const createdDate = new Date(record.createdAtRaw || record.createdAt);
    if (!Number.isNaN(createdDate.getTime())) {
      if (!summary.latestAt || createdDate > summary.latestAt) {
        summary.latestAt = createdDate;
      }
    }
  });

  return summary;
}

function renderTransactionWorkspace(records) {
  if (!records.length) {
    return renderWorkspaceLayout({
      title: 'Transaction Workspace',
      subtitle: 'No transactions available for the selected criteria.',
      bodyHtml: '<p class="result-empty">No transactions found.</p>',
    });
  }

  const summary = summarizeTransactions(records);

  const tableHtml = `
    <div class="result-table-wrap">
      <table class="result-table ledger-table">
        <thead>
          <tr>
            <th>Txn ID</th>
            <th>Type</th>
            <th>Status</th>
            <th>Amount</th>
            <th>Category</th>
            <th>Route</th>
            <th>Payment</th>
            <th>Account Flow</th>
            <th>Counterparty</th>
            <th>Created</th>
          </tr>
        </thead>
        <tbody>
          ${records.map(record => {
            const source = record.sourceAccountId !== '-' ? `#${record.sourceAccountId}` : '-';
            const destination = record.destinationAccountId !== '-' ? `#${record.destinationAccountId}` : (record.destinationValue !== '-' ? record.destinationValue : '-');
            const accountFlow = `${source} → ${destination}`;
            return `
              <tr>
                <td>${escapeHtml(record.id)}</td>
                <td>${renderTransactionTypePill(record.transactionType)}</td>
                <td>${renderStatusPill(record.status)}</td>
                <td class="amount-cell">${escapeHtml(formatCurrency(record.amount))}</td>
                <td>${escapeHtml(record.category || '-')}</td>
                <td>${escapeHtml(record.transferType || '-')}</td>
                <td>${escapeHtml(record.paymentMethod || '-')}</td>
                <td>${escapeHtml(accountFlow)}</td>
                <td>${escapeHtml(record.receiverName || '-')}</td>
                <td>${escapeHtml(record.createdAt)}</td>
              </tr>
            `;
          }).join('')}
        </tbody>
      </table>
    </div>
  `;

  return renderWorkspaceLayout({
    title: 'Transaction Workspace',
    subtitle: 'Transaction ledger with operational and financial context.',
    metrics: [
      { label: 'Records', value: formatNumber(summary.totalCount), tone: 'neutral' },
      { label: 'Credits', value: `${formatNumber(summary.creditCount)} (${formatCurrency(summary.creditAmount)})`, tone: 'success' },
      { label: 'Debits', value: `${formatNumber(summary.debitCount)} (${formatCurrency(summary.debitAmount)})`, tone: 'warning' },
      { label: 'Net Flow', value: formatCurrency(summary.creditAmount - summary.debitAmount), tone: (summary.creditAmount - summary.debitAmount) >= 0 ? 'success' : 'danger' },
    ],
    highlights: summary.latestAt ? [`Last activity ${summary.latestAt.toLocaleString()}`] : [],
    bodyHtml: tableHtml,
  });
}

function normalizeWalletTransactionRecord(item) {
  if (!item || typeof item !== 'object') return null;
  if (!('walletId' in item) || !('amount' in item) || !('type' in item)) return null;

  return {
    id: item.id ?? '-',
    walletId: item.walletId ?? '-',
    type: item.type ?? '-',
    amount: item.amount ?? 0,
    category: item.category ?? '-',
    description: item.description ?? '-',
    createdAtRaw: item.createdAt ?? null,
    createdAt: formatDateTime(item.createdAt),
  };
}

function extractWalletTransactionRecords(data) {
  if (Array.isArray(data)) {
    return data.map(normalizeWalletTransactionRecord).filter(Boolean);
  }
  if (data && typeof data === 'object') {
    const single = normalizeWalletTransactionRecord(data);
    if (single) return [single];
    if (Array.isArray(data.content)) {
      return data.content.map(normalizeWalletTransactionRecord).filter(Boolean);
    }
  }
  return [];
}

function isWalletTransactionPage(data) {
  return Boolean(data && typeof data === 'object' && Array.isArray(data.content) && 'totalElements' in data);
}

function walletTransactionTone(type) {
  const normalized = String(type || '').toUpperCase();
  if (normalized.includes('DEPOSIT') || normalized.includes('CREDIT') || normalized.includes('IN')) return 'success';
  if (normalized.includes('WITHDRAW') || normalized.includes('DEBIT') || normalized.includes('OUT')) return 'warning';
  return 'neutral';
}

function summarizeWalletTransactions(records) {
  let inflow = 0;
  let outflow = 0;
  records.forEach(record => {
    const amount = amountOfTransaction(record);
    const tone = walletTransactionTone(record.type);
    if (tone === 'success') inflow += amount;
    if (tone === 'warning') outflow += amount;
  });
  return {
    count: records.length,
    inflow,
    outflow,
  };
}

function renderWalletTransactionWorkspace(records, pageMeta = null) {
  if (!records.length) {
    return renderWorkspaceLayout({
      title: 'Wallet Ledger',
      subtitle: 'No wallet transactions found for the selected range.',
      bodyHtml: '<p class="result-empty">No wallet ledger entries found.</p>',
    });
  }

  const summary = summarizeWalletTransactions(records);
  const tableHtml = `
    <div class="result-table-wrap">
      <table class="result-table ledger-table">
        <thead>
          <tr>
            <th>Entry ID</th>
            <th>Wallet</th>
            <th>Type</th>
            <th>Amount</th>
            <th>Category</th>
            <th>Description</th>
            <th>Created</th>
          </tr>
        </thead>
        <tbody>
          ${records.map(record => `
            <tr>
              <td>${escapeHtml(record.id)}</td>
              <td>${escapeHtml(`#${record.walletId}`)}</td>
              <td><span class="status-pill ${walletTransactionTone(record.type)}">${escapeHtml(record.type || '-')}</span></td>
              <td class="amount-cell">${escapeHtml(formatCurrency(record.amount))}</td>
              <td>${escapeHtml(record.category || '-')}</td>
              <td>${escapeHtml(record.description || '-')}</td>
              <td>${escapeHtml(record.createdAt)}</td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `;

  const highlights = [];
  if (pageMeta) {
    highlights.push(`Page ${Number(pageMeta.number ?? 0) + 1} of ${Math.max(Number(pageMeta.totalPages ?? 1), 1)}`);
    highlights.push(`${formatNumber(pageMeta.totalElements ?? records.length)} total records`);
  }

  return renderWorkspaceLayout({
    title: 'Wallet Ledger',
    subtitle: 'Wallet transaction history with debit/credit visibility.',
    metrics: [
      { label: 'Entries', value: formatNumber(summary.count), tone: 'neutral' },
      { label: 'Inflow', value: formatCurrency(summary.inflow), tone: 'success' },
      { label: 'Outflow', value: formatCurrency(summary.outflow), tone: 'warning' },
      { label: 'Net Flow', value: formatCurrency(summary.inflow - summary.outflow), tone: (summary.inflow - summary.outflow) >= 0 ? 'success' : 'danger' },
    ],
    highlights,
    bodyHtml: tableHtml,
  });
}

function normalizeUserRecord(item) {
  if (!item || typeof item !== 'object') return null;
  if (!('id' in item) || (!('email' in item) && !('username' in item) && !('fullName' in item))) return null;

  return {
    id: item.id ?? '-',
    username: item.username ?? '-',
    fullName: item.fullName ?? item.name ?? '-',
    email: item.email ?? '-',
    role: item.role ?? 'USER',
    createdAt: formatDateTime(item.createdAt),
  };
}

function extractUserRecords(data) {
  if (Array.isArray(data)) return data.map(normalizeUserRecord).filter(Boolean);
  if (data && typeof data === 'object') {
    const single = normalizeUserRecord(data);
    if (single) return [single];
    if (Array.isArray(data.content)) return data.content.map(normalizeUserRecord).filter(Boolean);
  }
  return [];
}

function summarizeUsers(records) {
  const total = records.length;
  const adminCount = records.filter(item => String(item.role || '').toUpperCase() === 'ADMIN').length;
  return {
    total,
    adminCount,
    userCount: Math.max(total - adminCount, 0),
  };
}

function renderUserWorkspace(records, action = 'users') {
  if (!records.length) {
    return renderWorkspaceLayout({
      title: 'User Workspace',
      subtitle: 'No user records found.',
      bodyHtml: '<p class="result-empty">No user profiles available.</p>',
    });
  }

  const summary = summarizeUsers(records);
  const tableHtml = `
    <div class="result-table-wrap">
      <table class="result-table ledger-table">
        <thead>
          <tr>
            <th>User ID</th>
            <th>Username</th>
            <th>Full Name</th>
            <th>Email</th>
            <th>Role</th>
            <th>Created</th>
          </tr>
        </thead>
        <tbody>
          ${records.map(record => `
            <tr>
              <td>${escapeHtml(record.id)}</td>
              <td>${escapeHtml(record.username)}</td>
              <td>${escapeHtml(record.fullName)}</td>
              <td>${escapeHtml(record.email)}</td>
              <td>${renderStatusPill(record.role)}</td>
              <td>${escapeHtml(record.createdAt)}</td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `;

  const subtitle = action === 'update-user'
    ? 'Profile update receipt and current user snapshot.'
    : 'User directory and role visibility.';

  return renderWorkspaceLayout({
    title: 'User Workspace',
    subtitle,
    metrics: [
      { label: 'Profiles', value: formatNumber(summary.total), tone: 'neutral' },
      { label: 'Admins', value: formatNumber(summary.adminCount), tone: 'warning' },
      { label: 'Users', value: formatNumber(summary.userCount), tone: 'success' },
    ],
    bodyHtml: tableHtml,
  });
}

function normalizeTransferRecord(item) {
  if (!item || typeof item !== 'object') return null;
  if (!('amount' in item) || !('transferType' in item)) return null;

  const destinationAccount = item.destinationAccountId != null ? `Account #${item.destinationAccountId}` : null;
  const destinationWallet = item.destinationWalletId != null ? `Wallet #${item.destinationWalletId}` : null;
  const destinationValue = item.destinationValue || null;

  return {
    id: item.id ?? '-',
    transferType: item.transferType ?? '-',
    status: item.transferStatus ?? item.status ?? '-',
    sourceAccountId: item.sourceAccountId ?? '-',
    destination: destinationAccount || destinationWallet || destinationValue || '-',
    receiverName: item.receiverName ?? '-',
    paymentMethod: item.paymentMethod ?? '-',
    amount: item.amount ?? 0,
    createdAt: formatDateTime(item.createdAt),
  };
}

function extractTransferRecords(data) {
  if (Array.isArray(data)) return data.map(normalizeTransferRecord).filter(Boolean);
  if (data && typeof data === 'object') {
    const single = normalizeTransferRecord(data);
    if (single) return [single];
    if (Array.isArray(data.content)) return data.content.map(normalizeTransferRecord).filter(Boolean);
  }
  return [];
}

function renderTransferWorkspace(records) {
  if (!records.length) {
    return renderWorkspaceLayout({
      title: 'Transfer Workspace',
      subtitle: 'No transfer entries found.',
      bodyHtml: '<p class="result-empty">No transfer records available.</p>',
    });
  }

  const totalAmount = records.reduce((sum, item) => sum + amountOf(item.amount), 0);
  const tableHtml = `
    <div class="result-table-wrap">
      <table class="result-table ledger-table">
        <thead>
          <tr>
            <th>Transfer ID</th>
            <th>Route</th>
            <th>Status</th>
            <th>Amount</th>
            <th>Source Account</th>
            <th>Destination</th>
            <th>Receiver</th>
            <th>Payment</th>
            <th>Created</th>
          </tr>
        </thead>
        <tbody>
          ${records.map(record => `
            <tr>
              <td>${escapeHtml(record.id)}</td>
              <td>${escapeHtml(record.transferType)}</td>
              <td>${renderStatusPill(record.status)}</td>
              <td class="amount-cell">${escapeHtml(formatCurrency(record.amount))}</td>
              <td>${escapeHtml(record.sourceAccountId !== '-' ? `#${record.sourceAccountId}` : '-')}</td>
              <td>${escapeHtml(record.destination)}</td>
              <td>${escapeHtml(record.receiverName)}</td>
              <td>${escapeHtml(record.paymentMethod)}</td>
              <td>${escapeHtml(record.createdAt)}</td>
            </tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `;

  return renderWorkspaceLayout({
    title: 'Transfer Workspace',
    subtitle: 'Transfer routing and execution receipt.',
    metrics: [
      { label: 'Transfers', value: formatNumber(records.length), tone: 'neutral' },
      { label: 'Total Amount', value: formatCurrency(totalAmount), tone: 'success' },
    ],
    bodyHtml: tableHtml,
  });
}

function renderTransactionTable(records) {
  return renderTransactionWorkspace(records);
}

function renderBankAccountCards(records) {
  const normalizedRecords = records.map(normalizeBankAccountRecord).filter(Boolean);

  if (!normalizedRecords.length) {
    return '<p class="result-empty">No records found.</p>';
  }

  return `
    <div class="account-result-cards">
      ${normalizedRecords.map(account => `
        <article class="account-result-card">
          <p><strong>Bank Account ID:</strong> ${escapeHtml(account.id ?? '-')}</p>
          <p><strong>User ID:</strong> ${escapeHtml(account.userId ?? '-')}</p>
          <p><strong>Bank Name:</strong> ${escapeHtml(account.bankName ?? '-')}</p>
          <p><strong>Account Number:</strong> ${escapeHtml(account.accountNumber ?? '-')}</p>
          <p><strong>Balance:</strong> ${escapeHtml(formatCurrency(account.balance))}</p>
          <p><strong>Created At:</strong> ${escapeHtml(formatDateTime(account.createdAt))}</p>
        </article>
      `).join('')}
    </div>
  `;
}

function isReportOverview(data) {
  return Boolean(
    data &&
    typeof data === 'object' &&
    Array.isArray(data.bankBalances) &&
    (data.walletBalances == null || Array.isArray(data.walletBalances)) &&
    Array.isArray(data.monthlyExpenses) &&
    Array.isArray(data.expenseByCategory) &&
    data.incomeExpenseSummary &&
    typeof data.incomeExpenseSummary === 'object'
  );
}

function renderReportSummaryCards(summary) {
  const cards = [
    { label: 'Total Income', value: formatCurrency(summary?.totalIncome) },
    { label: 'Total Expense', value: formatCurrency(summary?.totalExpense) },
    { label: 'Net Savings', value: formatCurrency(summary?.netSavings) },
  ];

  return `
    <section class="report-overview-section">
      <h4>Income vs Expense Summary</h4>
      <div class="report-summary-grid">
        ${cards.map(card => `
          <article class="report-summary-card">
            <p>${escapeHtml(card.label)}</p>
            <strong>${escapeHtml(card.value)}</strong>
          </article>
        `).join('')}
      </div>
    </section>
  `;
}

function amountOf(value) {
  const numeric = Number(value ?? 0);
  return Number.isFinite(numeric) ? numeric : 0;
}

function reportPalette(index) {
  const colors = ['#4c88ff', '#1fc3b2', '#f59e0b', '#ef4444', '#8b5cf6', '#22c55e', '#ec4899', '#14b8a6'];
  return colors[index % colors.length];
}

function monthOrderValue(monthLabel) {
  const key = String(monthLabel || '').trim().toLowerCase();
  const map = new Map([
    ['january', 1], ['february', 2], ['march', 3], ['april', 4], ['may', 5], ['june', 6],
    ['july', 7], ['august', 8], ['september', 9], ['october', 10], ['november', 11], ['december', 12],
    ['jan', 1], ['feb', 2], ['mar', 3], ['apr', 4], ['jun', 6], ['jul', 7], ['aug', 8], ['sep', 9], ['oct', 10], ['nov', 11], ['dec', 12],
  ]);
  return map.get(key) ?? 99;
}

function normalizeCategorySeries(records) {
  if (!Array.isArray(records)) return [];
  return records
    .map(row => ({
      label: row?.category || 'Uncategorized',
      value: amountOf(row?.totalExpense),
    }))
    .filter(item => item.value > 0);
}

function normalizeWalletSeries(records) {
  if (!Array.isArray(records)) return [];
  return records
    .map(row => ({
      label: row?.walletName || row?.name || 'Wallet',
      value: amountOf(row?.balance),
      currency: row?.currency || 'INR',
    }))
    .filter(item => item.value > 0);
}

function normalizeMonthlySeries(records) {
  if (!Array.isArray(records)) return [];
  return records
    .map(row => ({
      month: row?.month || 'Unknown',
      year: Number(row?.year ?? 0),
      value: amountOf(row?.totalExpense),
    }))
    .sort((a, b) => (a.year - b.year) || (monthOrderValue(a.month) - monthOrderValue(b.month)));
}

function buildConicGradient(series) {
  const total = series.reduce((sum, item) => sum + item.value, 0);
  if (total <= 0) {
    return 'rgba(120, 144, 177, 0.25) 0deg 360deg';
  }
  let progress = 0;
  return series.map((item, index) => {
    const start = (progress / total) * 360;
    progress += item.value;
    const end = (progress / total) * 360;
    return `${reportPalette(index)} ${start.toFixed(2)}deg ${end.toFixed(2)}deg`;
  }).join(', ');
}

function renderChartLegend(series, total) {
  return `
    <div class="report-chart-legend">
      ${series.map((item, index) => {
        const percent = total > 0 ? (item.value / total) * 100 : 0;
        return `
          <div class="report-chart-legend-item">
            <span class="swatch" style="--swatch:${reportPalette(index)};"></span>
            <span class="label">${escapeHtml(item.label)}</span>
            <span class="value">${escapeHtml(formatCurrency(item.value))}</span>
            <span class="percent">${escapeHtml(percent.toFixed(1))}%</span>
          </div>
        `;
      }).join('')}
    </div>
  `;
}

function renderEmptyChartCard(title, message) {
  return `
    <article class="report-chart-card">
      <h5>${escapeHtml(title)}</h5>
      <p class="result-empty">${escapeHtml(message)}</p>
    </article>
  `;
}

function renderCategoryPieChart(records) {
  const series = normalizeCategorySeries(records);
  if (!series.length) {
    return renderEmptyChartCard('Pie Chart (Category)', 'No category expenses found.');
  }
  const total = series.reduce((sum, item) => sum + item.value, 0);
  return `
    <article class="report-chart-card">
      <h5>Pie Chart (Category)</h5>
      <div class="report-chart-body">
        <div class="chart-circle chart-pie" style="background: conic-gradient(${buildConicGradient(series)});"></div>
        <div class="chart-total">
          <span>Total Expense</span>
          <strong>${escapeHtml(formatCurrency(total))}</strong>
        </div>
      </div>
      ${renderChartLegend(series, total)}
    </article>
  `;
}

function renderWalletDonutChart(records) {
  const series = normalizeWalletSeries(records);
  if (!series.length) {
    return renderEmptyChartCard('Donut Chart (Wallets)', 'No wallet balances found.');
  }
  const total = series.reduce((sum, item) => sum + item.value, 0);
  return `
    <article class="report-chart-card">
      <h5>Donut Chart (Wallets)</h5>
      <div class="report-chart-body">
        <div class="chart-circle chart-donut" style="background: conic-gradient(${buildConicGradient(series)});">
          <div class="chart-donut-center">
            <span>${escapeHtml(String(series.length))} Wallets</span>
            <strong>${escapeHtml(formatCurrency(total))}</strong>
          </div>
        </div>
      </div>
      ${renderChartLegend(series, total)}
    </article>
  `;
}

function renderMonthlyLineChart(records) {
  const series = normalizeMonthlySeries(records).filter(item => item.value >= 0);
  if (!series.length) {
    return renderEmptyChartCard('Line Chart (Monthly Trend)', 'No monthly expense trend data found.');
  }

  const width = 620;
  const height = 240;
  const padding = 28;
  const plotWidth = width - (padding * 2);
  const plotHeight = height - (padding * 2);
  const maxValue = Math.max(...series.map(item => item.value), 1);
  const x = index => series.length === 1
    ? padding + (plotWidth / 2)
    : padding + (index * (plotWidth / (series.length - 1)));
  const y = value => padding + (plotHeight - ((value / maxValue) * plotHeight));
  const points = series.map((point, index) => `${x(index).toFixed(2)},${y(point.value).toFixed(2)}`).join(' ');

  return `
    <article class="report-chart-card">
      <h5>Line Chart (Monthly Trend)</h5>
      <div class="report-line-chart-wrap">
        <svg viewBox="0 0 ${width} ${height}" class="report-line-chart" role="img" aria-label="Monthly expense trend line chart">
          ${Array.from({ length: 5 }, (_, i) => {
            const ratio = i / 4;
            const yPos = padding + (ratio * plotHeight);
            const tickValue = maxValue * (1 - ratio);
            return `
              <line x1="${padding}" y1="${yPos.toFixed(2)}" x2="${(width - padding).toFixed(2)}" y2="${yPos.toFixed(2)}" class="grid-line"></line>
              <text x="4" y="${(yPos + 4).toFixed(2)}" class="axis-label">${escapeHtml(formatNumber(Math.round(tickValue)))}</text>
            `;
          }).join('')}
          <polyline points="${points}" class="trend-line"></polyline>
          ${series.map((point, index) => `
            <circle cx="${x(index).toFixed(2)}" cy="${y(point.value).toFixed(2)}" r="4" class="trend-dot"></circle>
          `).join('')}
        </svg>
      </div>
      <div class="report-line-labels">
        ${series.map(point => `
          <span>${escapeHtml(`${point.month.slice(0, 3)} ${point.year}`)}</span>
        `).join('')}
      </div>
    </article>
  `;
}

function renderIncomeExpenseBarChart(summary) {
  const income = amountOf(summary?.totalIncome);
  const expense = amountOf(summary?.totalExpense);
  const maxValue = Math.max(income, expense, 1);
  const bars = [
    { label: 'Income', value: income, tone: 'income' },
    { label: 'Expense', value: expense, tone: 'expense' },
  ];
  return `
    <article class="report-chart-card">
      <h5>Bar Chart (Income vs Expense)</h5>
      <div class="report-bar-chart">
        ${bars.map(bar => `
          <div class="report-bar-column">
            <div class="bar-track">
              <div class="bar-fill ${bar.tone}" style="height:${((bar.value / maxValue) * 100).toFixed(2)}%;"></div>
            </div>
            <p>${escapeHtml(bar.label)}</p>
            <strong>${escapeHtml(formatCurrency(bar.value))}</strong>
          </div>
        `).join('')}
      </div>
    </article>
  `;
}

function renderReportTable(title, columns, records, emptyMessage) {
  if (!Array.isArray(records) || !records.length) {
    return `
      <section class="report-overview-section">
        <h4>${escapeHtml(title)}</h4>
        <p class="result-empty">${escapeHtml(emptyMessage)}</p>
      </section>
    `;
  }

  return `
    <section class="report-overview-section">
      <h4>${escapeHtml(title)}</h4>
      <div class="result-table-wrap">
        <table class="result-table">
          <thead>
            <tr>${columns.map(col => `<th>${escapeHtml(col.label)}</th>`).join('')}</tr>
          </thead>
          <tbody>
            ${records.map(row => `
              <tr>
                ${columns.map(col => {
                  const value = typeof col.value === 'function' ? col.value(row) : row?.[col.value];
                  return `<td>${escapeHtml(value ?? '-')}</td>`;
                }).join('')}
              </tr>
            `).join('')}
          </tbody>
        </table>
      </div>
    </section>
  `;
}

function renderOverview(data) {
  const walletBalances = Array.isArray(data.walletBalances) ? data.walletBalances : [];
  return `
    <div class="report-overview">
      ${renderReportSummaryCards(data.incomeExpenseSummary)}
      <section class="report-overview-section">
        <h4>User Analytics</h4>
        <div class="report-charts-grid">
          ${renderCategoryPieChart(data.expenseByCategory)}
          ${renderMonthlyLineChart(data.monthlyExpenses)}
          ${renderIncomeExpenseBarChart(data.incomeExpenseSummary)}
          ${renderWalletDonutChart(walletBalances)}
        </div>
      </section>
      ${renderReportTable(
        'Bank Balances',
        [
          { label: 'Bank', value: row => row.bankName },
          { label: 'Account Number', value: row => row.accountNumber },
          { label: 'Balance', value: row => formatCurrency(row.balance) },
        ],
        data.bankBalances,
        'No bank balances found.'
      )}
      ${renderReportTable(
        'Wallet Balances',
        [
          { label: 'Wallet', value: row => row.walletName || row.name },
          { label: 'Currency', value: row => row.currency || 'INR' },
          { label: 'Balance', value: row => formatCurrency(row.balance) },
        ],
        walletBalances,
        'No wallet balances found.'
      )}
      ${renderReportTable(
        'Monthly Expenses',
        [
          { label: 'Month', value: row => row.month },
          { label: 'Year', value: row => row.year },
          { label: 'Total Expense', value: row => formatCurrency(row.totalExpense) },
        ],
        data.monthlyExpenses,
        'No monthly expenses found.'
      )}
      ${renderReportTable(
        'Expense by Category',
        [
          { label: 'Category', value: row => row.category || 'Uncategorized' },
          { label: 'Total Expense', value: row => formatCurrency(row.totalExpense) },
        ],
        data.expenseByCategory,
        'No category expense data found.'
      )}
    </div>
  `;
}

function isWalletTransferResponse(data) {
  return Boolean(
    data &&
    typeof data === 'object' &&
    data.debitTransaction &&
    data.creditTransaction &&
    'fromWalletBalance' in data &&
    'toWalletBalance' in data
  );
}

function renderWalletTransferResponse(data) {
  const fromWalletId = data?.fromWalletId ?? data?.debitTransaction?.walletId ?? '-';
  const toWalletId = data?.toWalletId ?? data?.creditTransaction?.walletId ?? '-';
  const amount = data?.amount ?? data?.debitTransaction?.amount ?? 0;
  const transferredAt = data?.transferredAt ?? data?.creditTransaction?.createdAt ?? data?.debitTransaction?.createdAt;

  const body = `
    <div class="kv-grid">
      <div class="kv-item"><div class="kv-key">From Wallet</div><div class="kv-val">${escapeHtml(fromWalletId)}</div></div>
      <div class="kv-item"><div class="kv-key">To Wallet</div><div class="kv-val">${escapeHtml(toWalletId)}</div></div>
      <div class="kv-item"><div class="kv-key">Transferred At</div><div class="kv-val">${escapeHtml(formatDateTime(transferredAt))}</div></div>
    </div>
    <div class="result-table-wrap">
      <table class="result-table ledger-table">
        <thead>
          <tr>
            <th>Entry</th>
            <th>Wallet</th>
            <th>Type</th>
            <th>Amount</th>
            <th>Category</th>
            <th>Description</th>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>${escapeHtml(data?.debitTransaction?.id ?? '-')}</td>
            <td>${escapeHtml(`#${data?.debitTransaction?.walletId ?? '-'}`)}</td>
            <td><span class="status-pill warning">${escapeHtml(data?.debitTransaction?.type ?? 'DEBIT')}</span></td>
            <td class="amount-cell">${escapeHtml(formatCurrency(data?.debitTransaction?.amount))}</td>
            <td>${escapeHtml(data?.debitTransaction?.category ?? '-')}</td>
            <td>${escapeHtml(data?.debitTransaction?.description ?? '-')}</td>
          </tr>
          <tr>
            <td>${escapeHtml(data?.creditTransaction?.id ?? '-')}</td>
            <td>${escapeHtml(`#${data?.creditTransaction?.walletId ?? '-'}`)}</td>
            <td><span class="status-pill success">${escapeHtml(data?.creditTransaction?.type ?? 'CREDIT')}</span></td>
            <td class="amount-cell">${escapeHtml(formatCurrency(data?.creditTransaction?.amount))}</td>
            <td>${escapeHtml(data?.creditTransaction?.category ?? '-')}</td>
            <td>${escapeHtml(data?.creditTransaction?.description ?? '-')}</td>
          </tr>
        </tbody>
      </table>
    </div>
  `;

  return renderWorkspaceLayout({
    title: 'Wallet Transfer Receipt',
    subtitle: 'Dual-entry transfer captured with balance updates.',
    metrics: [
      { label: 'Transfer Amount', value: formatCurrency(amount), tone: 'neutral' },
      { label: 'From Balance', value: formatCurrency(data?.fromWalletBalance), tone: 'warning' },
      { label: 'To Balance', value: formatCurrency(data?.toWalletBalance), tone: 'success' },
    ],
    highlights: [`Route #${fromWalletId} → #${toWalletId}`],
    bodyHtml: body,
  });
}

function renderResult(data, action = 'request', statusCode = 200) {
  const resultEl = currentResultEl();
  if (!resultEl) return;
  const userActions = new Set(['update-user', 'admin-users', 'admin-create-user', 'admin-update-user']);
  const transferActions = new Set(['transfer-funds']);

  if (isReportOverview(data)) {
    resultEl.innerHTML = renderWorkspaceLayout({
      title: 'Report Workspace',
      subtitle: 'Comprehensive analytics overview for financial decision-making.',
      bodyHtml: renderOverview(data),
      metrics: [
        { label: 'Bank Accounts', value: formatNumber(Array.isArray(data.bankBalances) ? data.bankBalances.length : 0), tone: 'neutral' },
        { label: 'Wallets', value: formatNumber(Array.isArray(data.walletBalances) ? data.walletBalances.length : 0), tone: 'success' },
        { label: 'Monthly Points', value: formatNumber(Array.isArray(data.monthlyExpenses) ? data.monthlyExpenses.length : 0), tone: 'warning' },
      ],
    });
    return;
  }

  if (isWalletTransferResponse(data)) {
    resultEl.innerHTML = renderWalletTransferResponse(data);
    return;
  }

  if (transferActions.has(action)) {
    const transferRecords = extractTransferRecords(data);
    if (transferRecords.length) {
      resultEl.innerHTML = renderTransferWorkspace(transferRecords);
      return;
    }
  }

  if (userActions.has(action)) {
    const userRecords = extractUserRecords(data);
    if (userRecords.length) {
      resultEl.innerHTML = renderUserWorkspace(userRecords, action);
      return;
    }
  }

  const extractedTransactions = extractTransactionRecords(data);
  if (extractedTransactions.length) {
    resultEl.innerHTML = renderTransactionTable(extractedTransactions);
    return;
  }

  const extractedWalletTransactions = extractWalletTransactionRecords(data);
  if (extractedWalletTransactions.length) {
    resultEl.innerHTML = renderWalletTransactionWorkspace(
      extractedWalletTransactions,
      isWalletTransactionPage(data) ? data : null
    );
    return;
  }

  const extractedAccounts = extractBankAccountRecords(data);
  if (extractedAccounts.length) {
    resultEl.innerHTML = renderWorkspaceLayout({
      title: 'Bank Accounts',
      subtitle: 'Account balances and ownership details.',
      bodyHtml: renderBankAccountCards(extractedAccounts),
      metrics: [
        { label: 'Accounts', value: formatNumber(extractedAccounts.length), tone: 'neutral' },
        { label: 'Total Balance', value: formatCurrency(extractedAccounts.reduce((sum, item) => sum + amountOf(item.balance), 0)), tone: 'success' },
      ],
    });
    return;
  }

  if (!Array.isArray(data) && isBankAccountRecord(data)) {
    resultEl.innerHTML = renderWorkspaceLayout({
      title: 'Bank Account',
      subtitle: 'Account detail snapshot.',
      bodyHtml: renderBankAccountCards([data]),
      metrics: [{ label: 'Balance', value: formatCurrency(data.balance), tone: 'success' }],
    });
    return;
  }

  const extractedUsers = extractUserRecords(data);
  if (extractedUsers.length) {
    resultEl.innerHTML = renderUserWorkspace(extractedUsers, action);
    return;
  }

  const extractedWallets = extractWalletRecords(data);
  if (extractedWallets.length) {
    resultEl.innerHTML = renderWorkspaceLayout({
      title: 'Wallets',
      subtitle: 'Wallet list with live balances.',
      bodyHtml: renderWalletCards(extractedWallets),
      metrics: [
        { label: 'Wallets', value: formatNumber(extractedWallets.length), tone: 'neutral' },
        { label: 'Total Wallet Balance', value: formatCurrency(extractedWallets.reduce((sum, item) => sum + amountOf(item.balance), 0)), tone: 'success' },
      ],
    });
    return;
  }

  if (data && typeof data === 'object' && Array.isArray(data.content)) {
    resultEl.innerHTML = renderWorkspaceLayout({
      title: 'Paginated Result',
      subtitle: 'Structured response with paging metadata.',
      bodyHtml: renderTable(data.content),
      highlights: [
        `Page ${Number(data.number ?? 0) + 1} of ${Math.max(Number(data.totalPages ?? 1), 1)}`,
        `${formatNumber(data.totalElements ?? data.content.length)} total records`,
      ],
    });
    return;
  }

  const body = Array.isArray(data) ? renderTable(data) : renderKeyValue(data);
  const isError = statusCode >= 400;
  resultEl.innerHTML = renderWorkspaceLayout({
    title: isError ? 'Operation Error' : humanizeKey(action || 'Result'),
    subtitle: isError ? 'Request failed. Review the details and retry.' : 'Operation result details.',
    bodyHtml: body,
    metrics: Array.isArray(data)
      ? [{ label: 'Records', value: formatNumber(data.length), tone: 'neutral' }]
      : [{ label: 'Fields', value: formatNumber(Object.keys(data || {}).length), tone: isError ? 'danger' : 'neutral' }],
  });
}

async function api(path, options = {}, action = 'request', config = {}) {
  const { renderResponse = true, toastSuccess = true, toastError = true } = config;
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 20000);

  const headers = {
    ...(options.headers || {}),
  };

  if (options.body != null && !Object.keys(headers).some(key => key.toLowerCase() === 'content-type')) {
    headers['Content-Type'] = 'application/json';
  }

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
      ? 'Unauthorized or session expired. Please login again.'
      : getErrorMessage(action, data, response.status);

    setStatus(message, false);
    if (toastError) {
      toast.error(message);
    }
    if (renderResponse) {
      renderResult(data, action, response.status);
    }
    if (responseDrawerEl) responseDrawerEl.open = true;
    if (response.status === 401) {
      logout(workspaceLoginRoute());
    }
    const err = new Error(message);
    err.toastShown = toastError;
    throw err;
  }

  const successMessage = getSuccessMessage(action, data);
  setStatus(successMessage, true);
  if (toastSuccess) {
    toast.success(`✅ ${successMessage}`);
  }
  if (renderResponse) {
    renderResult(data, action, response.status);
  }
  return data;
}

async function apiDownload(path, action, fileName) {
  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), 20000);

  try {
    const response = await fetch(`${baseUrl()}${path}`, {
      method: 'GET',
      headers: {
        Authorization: `Bearer ${currentToken()}`,
      },
      signal: controller.signal,
    });

    if (!response.ok) {
      let errorPayload = null;
      try {
        errorPayload = await response.json();
      } catch {
        errorPayload = null;
      }
      const message = response.status === 401
        ? 'Unauthorized or session expired. Please login again.'
        : getErrorMessage(action, errorPayload, response.status);
      setStatus(message, false);
      toast.error(message);
      if (response.status === 401) {
        logout(workspaceLoginRoute());
      }
      throw new Error(message);
    }

    const blob = await response.blob();
    const downloadUrl = window.URL.createObjectURL(blob);
    const anchor = document.createElement('a');
    anchor.href = downloadUrl;
    anchor.download = fileName;
    document.body.appendChild(anchor);
    anchor.click();
    anchor.remove();
    window.URL.revokeObjectURL(downloadUrl);

    const successMessage = getSuccessMessage(action, {});
    setStatus(successMessage, true);
    toast.success(`✅ ${successMessage}`);
    return { message: successMessage };
  } catch (error) {
    if (error.name === 'AbortError') {
      const message = 'Request timed out. Check backend status and try again.';
      setStatus(message, false);
      toast.error(message);
      throw new Error(message);
    }
    throw error;
  } finally {
    clearTimeout(timeout);
  }
}

async function loadAdminDashboard(notify = false) {
  if (!hasAdminDashboardShell()) return null;

  setAdminDashboardStatus('Loading dashboard...', 'loading');
  try {
    const data = await api('/admin/dashboard', {}, 'admin-dashboard', {
      renderResponse: false,
      toastSuccess: notify,
      toastError: notify,
    });
    renderAdminDashboard(data);
    setAdminDashboardStatus('Dashboard is in sync', 'success');
    return data;
  } catch (error) {
    const message = error instanceof Error ? error.message : 'Failed to load admin dashboard';
    setAdminDashboardStatus(message, 'error');
    throw error;
  }
}

async function handleAction(action) {
  const run = (path, options = {}) => api(path, options, action);

  switch (action) {
    case 'admin-dashboard':
      return loadAdminDashboard(true);

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
          userId: reportUserId(),
          bankName: requireText('ba-bankName', 'Bank name'),
          accountNumber: requireText('ba-accountNumber', 'Account number'),
          initialBalance: requireNumber('ba-initialBalance', 'Initial balance'),
        }),
      });

    case 'get-bank-account':
      return run(`/bank-accounts/${requireNumber('ba-account-id', 'Account ID')}`);

    case 'list-bank-accounts':
      return run(`/bank-accounts/user/${reportUserId()}`);

    case 'delete-bank-account':
      return run(`/bank-accounts/${requireNumber('ba-delete-id', 'Delete Account ID')}`, { method: 'DELETE' });

    case 'create-wallet': {
      const rawInitialBalance = val('w-initialBalance');
      const parsedInitialBalance = rawInitialBalance === '' ? 0 : Number(rawInitialBalance);
      if (Number.isNaN(parsedInitialBalance) || parsedInitialBalance < 0) {
        throw new Error('Initial balance cannot be negative');
      }

      return run('/wallets', {
        method: 'POST',
        body: JSON.stringify({
          name: requireText('w-name', 'Wallet name'),
          initialBalance: parsedInitialBalance,
          currency: val('w-currency') || 'INR',
        }),
      });
    }

    case 'list-wallets':
      return run('/wallets');

    case 'get-wallet':
      return run(`/wallets/${requireNumber('w-id-get', 'Wallet ID')}`);

    case 'delete-wallet':
      return run(`/wallets/${requireNumber('w-id-delete', 'Delete Wallet ID')}`, { method: 'DELETE' });

    case 'wallet-deposit':
      return run('/transactions/deposit', {
        method: 'POST',
        body: JSON.stringify({
          walletId: requireNumber('wt-deposit-walletId', 'Wallet ID'),
          amount: requireNumber('wt-deposit-amount', 'Deposit amount'),
          category: optionalText('wt-deposit-category'),
          description: optionalText('wt-deposit-description'),
        }),
      });

    case 'wallet-withdraw':
      return run('/transactions/withdraw', {
        method: 'POST',
        body: JSON.stringify({
          walletId: requireNumber('wt-withdraw-walletId', 'Wallet ID'),
          amount: requireNumber('wt-withdraw-amount', 'Withdraw amount'),
          category: optionalText('wt-withdraw-category'),
          description: optionalText('wt-withdraw-description'),
        }),
      });

    case 'wallet-transfer':
      return run('/transactions/transfer', {
        method: 'POST',
        body: JSON.stringify({
          fromWalletId: requireNumber('wt-transfer-fromWalletId', 'From wallet ID'),
          toWalletId: requireNumber('wt-transfer-toWalletId', 'To wallet ID'),
          amount: requireNumber('wt-transfer-amount', 'Transfer amount'),
          category: optionalText('wt-transfer-category'),
          description: optionalText('wt-transfer-description'),
        }),
      });

    case 'wallet-list-transactions': {
      const walletId = requireNumber('wt-history-walletId', 'Wallet ID');
      const rawPage = val('wt-history-page');
      const rawSize = val('wt-history-size');
      const page = rawPage === '' ? 0 : Number(rawPage);
      const size = rawSize === '' ? 20 : Number(rawSize);

      if (Number.isNaN(page) || page < 0) {
        throw new Error('Page must be zero or greater');
      }
      if (Number.isNaN(size) || size <= 0) {
        throw new Error('Size must be greater than zero');
      }

      return run(`/wallets/${walletId}/transactions?page=${page}&size=${size}`);
    }

    case 'record-income':
      return run('/transactions/income', {
        method: 'POST',
        body: JSON.stringify({
          bankAccountId: requireNumber('tx-income-bankAccountId', 'Income Bank Account ID'),
          amount: requireNumber('tx-income-amount', 'Income amount'),
          category: val('tx-income-category'),
          paymentMethod: requireText('tx-income-paymentMethod', 'Income payment method'),
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
          paymentMethod: requireText('tx-expense-paymentMethod', 'Expense payment method'),
          description: val('tx-expense-description'),
        }),
      });

    case 'clear-tx-filters':
      clearFieldValues([
        'tx-filter-fromDate',
        'tx-filter-toDate',
        'tx-filter-transactionType',
        'tx-filter-minAmount',
        'tx-filter-maxAmount',
        'tx-filter-status',
        'tx-filter-category',
        'tx-filter-paymentMethod',
        'tx-filter-accountId',
        'tx-filter-receiver',
      ]);
      setStatus('Transaction filters cleared', true);
      const resultEl = currentResultEl();
      if (resultEl) {
        resultEl.innerHTML = renderWorkspaceLayout({
          title: 'Filters Reset',
          subtitle: 'Transaction history filters were cleared.',
          bodyHtml: '<p class="result-empty">Set new criteria and load history again.</p>',
          metrics: [{ label: 'Updated', value: 'All filter inputs reset', tone: 'success' }],
        });
      }
      return {
        message: 'Transaction filters cleared',
        status: 200,
      };

    case 'list-transactions':
    {
      const userId = reportUserId();
      const params = new URLSearchParams();

      const fromDate = val('tx-filter-fromDate');
      const toDate = val('tx-filter-toDate');
      const transactionType = val('tx-filter-transactionType');
      const minAmount = val('tx-filter-minAmount');
      const maxAmount = val('tx-filter-maxAmount');
      const status = val('tx-filter-status');
      const category = val('tx-filter-category');
      const paymentMethod = val('tx-filter-paymentMethod');
      const accountId = val('tx-filter-accountId');
      const receiver = val('tx-filter-receiver');

      if (fromDate && toDate && fromDate > toDate) {
        throw new Error('From date cannot be later than To date');
      }

      if (minAmount && maxAmount && Number(minAmount) > Number(maxAmount)) {
        throw new Error('Min amount cannot be greater than Max amount');
      }

      if (fromDate) params.set('fromDate', fromDate);
      if (toDate) params.set('toDate', toDate);
      if (transactionType) params.set('transactionType', transactionType);
      if (minAmount) params.set('minAmount', minAmount);
      if (maxAmount) params.set('maxAmount', maxAmount);
      if (status) params.set('status', status);
      if (category) params.set('category', category);
      if (paymentMethod) params.set('paymentMethod', paymentMethod);
      if (accountId) params.set('accountId', accountId);
      if (receiver) params.set('receiver', receiver);

      const qs = params.toString();
      const path = qs
        ? `/transactions/user/${userId}?${qs}`
        : `/transactions/user/${userId}`;
      return run(path);
    }

    case 'get-transaction':
      return run(`/transactions/${requireNumber('tx-id', 'Transaction ID')}`);

    case 'transfer-funds': {
      const transferType = requireText('tr-transferType', 'Transfer type');
      const destinationAccountId = num('tr-destinationAccountId');
      const destinationWalletId = num('tr-destinationWalletId');
      const mobileNumber = val('tr-mobileNumber');
      const upiId = val('tr-upiId');
      const receiverName = val('tr-receiverName');
      const paymentMethod = val('tr-paymentMethod');
      const transferStatus = requireText('tr-transferStatus', 'Transfer status');
      const selfTransfer = document.getElementById('tr-selfTransfer')?.checked === true;

      return run('/transfers', {
        method: 'POST',
        body: JSON.stringify({
          sourceAccountId: requireNumber('tr-sourceAccountId', 'Source Account ID'),
          destinationAccountId,
          destinationWalletId,
          transferType,
          selfTransfer,
          mobileNumber,
          upiId,
          receiverName,
          paymentMethod,
          transferStatus,
          amount: requireNumber('tr-amount', 'Transfer amount'),
          description: val('tr-description'),
        }),
      });
    }


    case 'report-bank-balances':
      return run(reportPath('/bank-balances'));

    case 'report-monthly-expenses':
      return run(reportPath('/monthly-expenses'));

    case 'report-category-expense':
      return run(reportPath('/expense-by-category'));

    case 'report-summary':
      return run(reportPath('/income-expense-summary'));

    case 'report-overview':
      return run(reportPath('/overview'));

    case 'report-bank-statement-csv': {
      const fromDate = requireText('statement-from-date', 'Statement from date');
      const toDate = requireText('statement-to-date', 'Statement to date');
      if (fromDate > toDate) {
        throw new Error('Statement from date cannot be later than to date');
      }
      return apiDownload(
        reportPath('/bank-statement/export', { format: 'csv', fromDate, toDate }),
        action,
        `bank-statement-${fromDate}-to-${toDate}.csv`
      );
    }

    case 'report-bank-statement-pdf': {
      const fromDate = requireText('statement-from-date', 'Statement from date');
      const toDate = requireText('statement-to-date', 'Statement to date');
      if (fromDate > toDate) {
        throw new Error('Statement from date cannot be later than to date');
      }
      return apiDownload(
        reportPath('/bank-statement/export', { format: 'pdf', fromDate, toDate }),
        action,
        `bank-statement-${fromDate}-to-${toDate}.pdf`
      );
    }

    case 'report-overview-csv': {
      const generatedOn = new Date().toISOString().slice(0, 10);
      return apiDownload(
        reportPath('/overview/export', { format: 'csv' }),
        action,
        `overall-report-${generatedOn}.csv`
      );
    }

    case 'report-overview-pdf': {
      const generatedOn = new Date().toISOString().slice(0, 10);
      return apiDownload(
        reportPath('/overview/export', { format: 'pdf' }),
        action,
        `overall-report-${generatedOn}.pdf`
      );
    }

    case 'admin-users':
      return run('/admin/users');

    case 'admin-create-user':
      return run('/admin/users', {
        method: 'POST',
        body: JSON.stringify({
          username: requireText('admin-create-username', 'Username'),
          email: requireText('admin-create-email', 'Email'),
          fullName: requireText('admin-create-fullname', 'Full name'),
          password: requireText('admin-create-password', 'Password'),
        }),
      });

    case 'admin-update-user':
      return run(`/admin/users/${requireNumber('admin-update-id', 'User ID')}`, {
        method: 'PUT',
        body: JSON.stringify({
          username: requireText('admin-update-username', 'Username'),
          email: requireText('admin-update-email', 'Email'),
          fullName: requireText('admin-update-fullname', 'Full name'),
          password: optionalText('admin-update-password'),
          role: optionalText('admin-update-role'),
        }),
      });

    case 'admin-accounts':
      return run('/admin/accounts');

    case 'admin-update-account':
      return run(`/admin/accounts/${requireNumber('admin-account-id', 'Account ID')}`, {
        method: 'PUT',
        body: JSON.stringify({
          userId: requireNumber('admin-account-user-id', 'User ID'),
          bankName: requireText('admin-account-bank-name', 'Bank name'),
          accountNumber: requireText('admin-account-number', 'Account number'),
          balance: requireNumber('admin-account-balance', 'Balance'),
        }),
      });

    case 'admin-monthly-performance-csv': {
      const monthValue = requireText('admin-performance-month', 'Month');
      const [yearRaw, monthRaw] = monthValue.split('-');
      const year = Number(yearRaw);
      const month = Number(monthRaw);
      if (Number.isNaN(year) || Number.isNaN(month) || month < 1 || month > 12) {
        throw new Error('Month must be in YYYY-MM format');
      }

      return apiDownload(
        `/admin/performance/monthly/export?year=${year}&month=${month}&format=csv`,
        action,
        `admin-monthly-performance-${monthValue}.csv`
      );
    }

    case 'admin-monthly-performance-pdf': {
      const monthValue = requireText('admin-performance-month', 'Month');
      const [yearRaw, monthRaw] = monthValue.split('-');
      const year = Number(yearRaw);
      const month = Number(monthRaw);
      if (Number.isNaN(year) || Number.isNaN(month) || month < 1 || month > 12) {
        throw new Error('Month must be in YYYY-MM format');
      }

      return apiDownload(
        `/admin/performance/monthly/export?year=${year}&month=${month}&format=pdf`,
        action,
        `admin-monthly-performance-${monthValue}.pdf`
      );
    }

    case 'admin-system-report-csv': {
      const fromDate = requireText('admin-report-from-date', 'From date');
      const toDate = requireText('admin-report-to-date', 'To date');
      return apiDownload(
        `/admin/system/export?fromDate=${fromDate}&toDate=${toDate}&format=csv`,
        action,
        `admin-system-report-${new Date().toISOString().split('T')[0]}.csv`
      );
    }

    case 'admin-system-report-pdf': {
      const fromDate = requireText('admin-report-from-date', 'From date');
      const toDate = requireText('admin-report-to-date', 'To date');
      return apiDownload(
        `/admin/system/export?fromDate=${fromDate}&toDate=${toDate}&format=pdf`,
        action,
        `admin-system-report-${new Date().toISOString().split('T')[0]}.pdf`
      );
    }

    case 'admin-transaction-report-csv': {
      const fromDate = requireText('admin-report-from-date', 'From date');
      const toDate = requireText('admin-report-to-date', 'To date');
      return apiDownload(
        `/admin/transactions/export?fromDate=${fromDate}&toDate=${toDate}&format=csv`,
        action,
        `admin-transaction-report-${new Date().toISOString().split('T')[0]}.csv`
      );
    }

    case 'admin-transaction-report-pdf': {
      const fromDate = requireText('admin-report-from-date', 'From date');
      const toDate = requireText('admin-report-to-date', 'To date');
      return apiDownload(
        `/admin/transactions/export?fromDate=${fromDate}&toDate=${toDate}&format=pdf`,
        action,
        `admin-transaction-report-${new Date().toISOString().split('T')[0]}.pdf`
      );
    }

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

function bindTransferRouteUX() {
  const transferTypeEl = document.getElementById('tr-transferType');
  if (!(transferTypeEl instanceof HTMLSelectElement)) return;

  const paymentMethodEl = document.getElementById('tr-paymentMethod');
  const routeHintEl = document.getElementById('tr-routeHint');
  const selfTransferWrap = document.querySelector('.transfer-inline-check');
  const selfTransferInput = document.getElementById('tr-selfTransfer');
  const routeFields = Array.from(document.querySelectorAll('[data-transfer-field]'));

  const hints = {
    ACCOUNT: 'Route set to account transfer. Enter destination account ID.',
    WALLET: 'Route set to wallet transfer. Enter destination wallet ID.',
    MOBILE: 'Route set to mobile transfer. Enter receiver mobile number.',
    UPI: 'Route set to UPI transfer. Enter beneficiary UPI ID.'
  };

  const defaultPaymentMethod = route => {
    if (route === 'ACCOUNT') return 'NET_BANKING';
    if (route === 'WALLET') return 'WALLET';
    return 'UPI';
  };

  if (paymentMethodEl instanceof HTMLSelectElement) {
    paymentMethodEl.dataset.autoSet = paymentMethodEl.value ? 'false' : 'true';
    paymentMethodEl.addEventListener('change', () => {
      paymentMethodEl.dataset.autoSet = 'false';
    });
  }

  function applyRouteUX() {
    const route = transferTypeEl.value || 'ACCOUNT';
    routeFields.forEach(node => {
      if (!(node instanceof HTMLElement)) return;
      node.hidden = node.dataset.transferField !== route;
    });

    if (routeHintEl) {
      routeHintEl.textContent = hints[route] || hints.ACCOUNT;
    }

    if (paymentMethodEl instanceof HTMLSelectElement) {
      const shouldAutoApply = !paymentMethodEl.value || paymentMethodEl.dataset.autoSet === 'true';
      if (shouldAutoApply) {
        paymentMethodEl.value = defaultPaymentMethod(route);
        paymentMethodEl.dataset.autoSet = 'true';
      }
    }

    if (selfTransferWrap instanceof HTMLElement) {
      selfTransferWrap.hidden = route !== 'ACCOUNT';
    }
    if (route !== 'ACCOUNT' && selfTransferInput instanceof HTMLInputElement) {
      selfTransferInput.checked = false;
    }
  }

  transferTypeEl.addEventListener('change', applyRouteUX);
  applyRouteUX();
}

const yearEl = document.getElementById('year');
if (yearEl) {
  yearEl.textContent = String(new Date().getFullYear());
}

setStatus('Ready for actions', true);
bindTransferRouteUX();

if (logoutButton) {
  logoutButton.addEventListener('click', () => logout(workspaceLoginRoute()));
}

bootstrapSession();
function reportPath(path, params = {}) {
  const search = new URLSearchParams();

  if (isAdmin()) {
    search.set('userId', String(reportUserId()));
  }

  Object.entries(params).forEach(([key, value]) => {
    if (value == null || value === '') return;
    search.set(key, String(value));
  });

  const qs = search.toString();
  return qs ? `/reports${path}?${qs}` : `/reports${path}`;
}

const adminPerformanceMonthEl = document.getElementById('admin-performance-month');
if (adminPerformanceMonthEl && !adminPerformanceMonthEl.value) {
  const now = new Date();
  const yyyy = now.getFullYear();
  const mm = String(now.getMonth() + 1).padStart(2, '0');
  adminPerformanceMonthEl.value = `${yyyy}-${mm}`;
}
