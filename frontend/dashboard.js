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
const adminRecentTransactionsEl = document.getElementById('adminRecentTransactions');
const adminRecentUsersEl = document.getElementById('adminRecentUsers');
const adminRecentTransfersEl = document.getElementById('adminRecentTransfers');
const adminFailedTransfersEl = document.getElementById('adminFailedTransfers');
const adminLowBalancesEl = document.getElementById('adminLowBalances');
const adminSuspiciousTransactionsEl = document.getElementById('adminSuspiciousTransactions');

const actionFeedback = {
  'admin-dashboard': { success: 'Admin dashboard refreshed', error: 'Failed to load admin dashboard' },
  'update-user': { success: 'User profile updated successfully', error: 'Failed to update profile' },
  'create-bank-account': { success: 'Bank account created successfully', error: 'Failed to create bank account' },
  'get-bank-account': { success: 'Bank account loaded successfully', error: 'Failed to fetch bank account' },
  'list-bank-accounts': { success: 'Bank accounts loaded successfully', error: 'Failed to list bank accounts' },
  'delete-bank-account': { success: 'Bank account deleted successfully', error: 'Failed to delete bank account' },
  'record-income': { success: 'Income recorded successfully', error: 'Failed to record income' },
  'record-expense': { success: 'Expense recorded successfully', error: 'Failed to record expense' },
  'record-atm': { success: 'ATM withdrawal recorded', error: 'Failed to record ATM withdrawal' },
  'list-transactions': { success: 'Transactions loaded', error: 'Failed to list transactions' },
  'get-transaction': { success: 'Transaction loaded', error: 'Failed to fetch transaction' },
  'transfer-funds': { success: 'Transfer completed successfully', error: 'Failed to transfer funds' },
  'report-bank-balances': { success: 'Bank balance report generated', error: 'Failed to generate bank balance report' },
  'report-monthly-expenses': { success: 'Monthly expense report generated', error: 'Failed to generate monthly expense report' },
  'report-category-expense': { success: 'Category report generated', error: 'Failed to generate category report' },
  'report-summary': { success: 'Income/expense summary generated', error: 'Failed to generate income/expense summary' },
  'report-overview': { success: 'Financial overview generated', error: 'Failed to generate financial overview' },
  'admin-users': { success: 'Admin users loaded', error: 'Failed to load admin users' },
  'admin-create-user': { success: 'User profile created successfully', error: 'Failed to create user profile' },
  'admin-update-user': { success: 'User profile updated successfully', error: 'Failed to update user profile' },
  'admin-accounts': { success: 'Admin accounts loaded', error: 'Failed to load admin accounts' },
  'admin-update-account': { success: 'User bank account updated successfully', error: 'Failed to update user bank account' },
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

function reportUserId(id, label) {
  if (!isAdmin()) {
    const uid = currentUserId();
    if (!uid) {
      throw new Error('Unable to resolve current user session. Please login again.');
    }
    return uid;
  }
  return requireNumber(id, label);
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

    const path = window.location.pathname;
    if (path.endsWith('/transactions.html') || path.endsWith('/transfers.html')) {
      window.location.href = 'users.html';
    }
    return;
  }

  const userIdInputs = [
    'ba-userId',
    'ba-user-list',
    'tx-user-list',
    'r-userId'
  ];

  userIdInputs.forEach(id => {
    const input = document.getElementById(id);
    if (!input) return;
    input.value = String(currentUserId() || '');
    input.setAttribute('readonly', 'readonly');
  });

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
  if (normalized === 'SENT' || normalized === 'SUCCESS' || normalized === 'UP') return 'success';
  return 'neutral';
}

function renderStatusPill(status) {
  return `<span class="status-pill ${adminStatusTone(status)}">${escapeHtml(status || 'UNKNOWN')}</span>`;
}

function renderAdminMetricGrid(metrics) {
  if (!adminMetricGridEl || !metrics) return;

  const cards = [
    { label: 'Total Users', value: formatNumber(metrics.totalUsers), tone: 'users' },
    { label: 'Total Accounts / Wallets', value: formatNumber(metrics.totalAccounts), tone: 'accounts' },
    { label: 'Total Transactions', value: formatNumber(metrics.totalTransactions), tone: 'transactions' },
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
    { label: 'API / System', value: health.apiStatus || 'UNKNOWN', tone: adminStatusTone(health.apiStatus) },
  ];

  adminHealthGridEl.innerHTML = items.map(item => `
    <article class="health-card ${item.tone}">
      <p>${escapeHtml(item.label)}</p>
      <div class="health-value">${item.label === 'API / System' ? renderStatusPill(item.value) : `<strong>${escapeHtml(item.value)}</strong>`}</div>
    </article>
  `).join('');
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
}

function hasAdminDashboardShell() {
  return Boolean(adminMetricGridEl || adminSnapshotGridEl || adminHealthGridEl);
}

function renderAdminDashboard(data) {
  if (!hasAdminDashboardShell() || !data) return;

  renderAdminMetricGrid(data.keyMetrics);
  renderAdminSnapshot(data.snapshot);
  renderAdminHealth(data.systemHealth);
  renderRecentTransactions(data.recentActivity?.latestTransactions);
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
    createdAt: formatDateTime(item.createdAt),
  };
}

function extractTransactionRecords(data) {
  if (!Array.isArray(data)) return [];
  return data.map(normalizeTransactionRecord).filter(Boolean);
}

function renderTransactionTable(records) {
  if (!records.length) {
    return '<p class="result-empty">No records found.</p>';
  }

  const columns = [
    'id',
    'userId',
    'transactionType',
    'transferType',
    'selfTransfer',
    'sourceAccountId',
    'destinationAccountId',
    'destinationValue',
    'amount',
    'category',
    'status',
    'paymentMethod',
    'receiverName',
    'description',
    'createdAt',
  ];

  return `
    <div class="result-table-wrap">
      <table class="result-table">
        <thead><tr>${columns.map(c => `<th>${escapeHtml(c)}</th>`).join('')}</tr></thead>
        <tbody>
          ${records.map(row => `
            <tr>${columns.map(c => `<td>${escapeHtml(row[c] ?? '')}</td>`).join('')}</tr>
          `).join('')}
        </tbody>
      </table>
    </div>
  `;
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
          <p><strong>Bank Account Id -</strong> ${escapeHtml(account.id ?? '-')}</p>
          <p><strong>User Id -</strong> ${escapeHtml(account.userId ?? '-')}</p>
          <p><strong>Bank Name -</strong> ${escapeHtml(account.bankName ?? '-')}</p>
          <p><strong>Account Number -</strong> ${escapeHtml(account.accountNumber ?? '-')}</p>
          <p><strong>Balance -</strong> ₹ ${escapeHtml(account.balance ?? '-')}</p>
          <p><strong>Account Created At -</strong> ${escapeHtml(formatDateTime(account.createdAt))}</p>
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
  return `
    <div class="report-overview">
      ${renderReportSummaryCards(data.incomeExpenseSummary)}
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

function renderResult(data) {
  if (!resultEl) return;

  if (isReportOverview(data)) {
    resultEl.innerHTML = renderOverview(data);
    return;
  }

  const extractedTransactions = extractTransactionRecords(data);
  if (extractedTransactions.length) {
    resultEl.innerHTML = renderTransactionTable(extractedTransactions);
    return;
  }

  const extractedAccounts = extractBankAccountRecords(data);
  if (extractedAccounts.length) {
    resultEl.innerHTML = renderBankAccountCards(extractedAccounts);
    return;
  }

  if (!Array.isArray(data) && isBankAccountRecord(data)) {
    resultEl.innerHTML = renderBankAccountCards([data]);
    return;
  }

  const body = Array.isArray(data) ? renderTable(data) : renderKeyValue(data);
  resultEl.innerHTML = body;
}

async function api(path, options = {}, action = 'request', config = {}) {
  const { renderResponse = true, toastSuccess = true, toastError = true } = config;
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

    setStatus(message, false);
    if (toastError) {
      toast.error(message);
    }
    if (renderResponse) {
      renderResult(data);
    }
    if (responseDrawerEl) responseDrawerEl.open = true;
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
    renderResult(data);
  }
  return data;
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
          userId: roleUserId('ba-userId', 'User ID'),
          bankName: requireText('ba-bankName', 'Bank name'),
          accountNumber: requireText('ba-accountNumber', 'Account number'),
          initialBalance: requireNumber('ba-initialBalance', 'Initial balance'),
        }),
      });

    case 'get-bank-account':
      return run(`/bank-accounts/${requireNumber('ba-account-id', 'Account ID')}`);

    case 'list-bank-accounts':
      return run(`/bank-accounts/user/${roleUserId('ba-user-list', 'User ID')}`);

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

    case 'list-transactions':
    {
      const userId = roleUserId('tx-user-list', 'User ID');
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
    MOBILE: 'Route set to mobile transfer. Enter receiver mobile number.',
    UPI: 'Route set to UPI transfer. Enter beneficiary UPI ID.'
  };

  const defaultPaymentMethod = route => (route === 'ACCOUNT' ? 'NET_BANKING' : 'UPI');

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
function reportPath(path) {
  const userId = reportUserId('r-userId', 'User ID');
  const adminPathMap = {
    '/bank-balances': `/reports/users/${userId}/bank-balances`,
    '/monthly-expenses': `/reports/users/${userId}/expenses/monthly`,
    '/expense-by-category': `/reports/users/${userId}/expenses/by-category`,
    '/income-expense-summary': `/reports/users/${userId}/summary`,
    '/overview': `/reports/users/${userId}/overview`,
  };

  if (isAdmin()) {
    return adminPathMap[path] || `/reports${path}?userId=${userId}`;
  }

  return `/reports${path}`;
}
