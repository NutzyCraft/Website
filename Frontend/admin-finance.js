document.addEventListener('DOMContentLoaded', () => {
    const API_URL = '/api/admin/finance';
    const tableBody = document.querySelector('.dashboard-table tbody');

    // Stats Elements
    const elRevenue = document.querySelector('.stat-card:nth-child(1) .stat-value');
    const elPending = document.querySelector('.stat-card:nth-child(2) .stat-value');
    const elCommission = document.querySelector('.stat-card:nth-child(3) .stat-value');
    const exportBtn = document.getElementById('export-report-btn');

    if (exportBtn) {
        exportBtn.addEventListener('click', () => {
            window.location.href = '/api/admin/finance/export';
        });
    }

    async function fetchFinance() {
        try {
            const response = await fetch(API_URL);
            if (!response.ok) throw new Error('Failed to fetch finance data');
            const data = await response.json();

            updateStats(data);
            renderTransactions(data.recentTransactions);
        } catch (error) {
            console.error('Error fetching finance:', error);
            if (tableBody) tableBody.innerHTML = '<tr><td colspan="5" style="text-align:center; color:red;">Failed to load data.</td></tr>';
        }
    }

    function updateStats(data) {
        if (elRevenue) elRevenue.textContent = formatCurrency(data.totalRevenue);
        if (elPending) elPending.textContent = formatCurrency(data.pendingPayouts);
        if (elCommission) elCommission.textContent = formatCurrency(data.commissionEarnings);
    }

    function renderTransactions(transactions) {
        if (!tableBody) return;
        tableBody.innerHTML = '';

        transactions.forEach(trx => {
            const row = document.createElement('tr');
            row.style.borderBottom = '1px solid #f0f0f0';

            const amountClass = trx.type === 'CREDIT' ? 'color: #2e7d32;' : 'color: #d32f2f;';
            const sign = trx.type === 'CREDIT' ? '+' : '-';
            const statusBadge = getStatusBadge(trx.status);

            row.innerHTML = `
                <td style="padding: 15px;">#TRX-${trx.id}</td>
                <td>
                    <div style="font-weight: 600;">${escapeHtml(trx.description)}</div>
                    <div style="font-size: 0.8rem; color: #888;">${trx.type === 'CREDIT' ? 'From' : 'To'}: ${escapeHtml(trx.userName)}</div>
                </td>
                <td>${trx.date}</td>
                <td style="${amountClass}">${sign}${formatCurrency(trx.amount)}</td>
                <td>${statusBadge}</td>
            `;
            tableBody.appendChild(row);
        });
    }

    function getStatusBadge(status) {
        let style = 'background: #eee; color: #666;';
        if (status === 'PROCESSED' || status === 'RECEIVED') style = 'background: #e8f5e9; color: #2e7d32;'; // Active/Green
        else if (status === 'PENDING') style = 'background: #fff3e0; color: #e65100;'; // Warning/Orange

        // Match CSS classes if possible, else inline
        return `<span class="status-badge" style="${style}">${status}</span>`;
    }

    function formatCurrency(value) {
        return new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(value);
    }

    function escapeHtml(text) {
        if (!text) return '';
        return text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
    }

    fetchFinance();
});
