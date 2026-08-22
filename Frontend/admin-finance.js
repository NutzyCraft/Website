document.addEventListener('DOMContentLoaded', () => {
    const API_URL = `${API_CONFIG.BASE_URL}/api/admin/finance`;
    const PAYOUT_API_URL = `${API_CONFIG.BASE_URL}/api/admin/finance/payouts`;
    const txBody = document.getElementById('transactions-body');
    const payoutBody = document.getElementById('payout-queue-body');

    // Stats Elements
    const elRevenue = document.getElementById('total-revenue');
    const elPending = document.getElementById('pending-payouts');
    const elCommission = document.getElementById('net-profit');
    const exportBtn = document.getElementById('export-report-btn');

    if (exportBtn) {
        exportBtn.addEventListener('click', () => {
            window.location.href = `${API_CONFIG.BASE_URL}/api/admin/finance/export`;
        });
    }

    // ─── Finance Stats & Transactions ──────────────────────────────────────────

    async function fetchFinance() {
        try {
            const response = await fetch(API_URL);
            if (!response.ok) throw new Error('Failed to fetch finance data');
            const data = await response.json();

            updateStats(data);
            renderTransactions(data.recentTransactions);
        } catch (error) {
            console.error('Error fetching finance:', error);
            if (txBody) txBody.innerHTML = '<tr><td colspan="5" style="text-align:center; color:red;">Failed to load data.</td></tr>';
        }
    }

    function updateStats(data) {
        if (elRevenue) elRevenue.textContent = formatCurrency(data.totalRevenue);
        if (elPending) elPending.textContent = formatCurrency(data.pendingPayouts);
        if (elCommission) elCommission.textContent = formatCurrency(data.commissionEarnings);
    }

    function renderTransactions(transactions) {
        if (!txBody) return;
        txBody.innerHTML = '';

        if (!transactions || transactions.length === 0) {
            txBody.innerHTML = '<tr><td colspan="5" style="text-align:center; padding: 20px; color: #888;">No transactions yet.</td></tr>';
            return;
        }

        transactions.forEach(trx => {
            const row = document.createElement('tr');
            row.style.borderBottom = '1px solid #f0f0f0';

            // INBOUND_PAYHERE = green (money IN), OUTBOUND_MANUAL = red (money OUT)
            const isInbound = trx.type === 'INBOUND_PAYHERE';
            const amountClass = isInbound ? 'color: #2e7d32;' : 'color: #d32f2f;';
            const sign = isInbound ? '+' : '-';
            const typeLabel = isInbound ? 'PayHere Receipt' : 'Manual Payout';
            const statusBadge = getStatusBadge(trx.status);

            row.innerHTML = `
                <td style="padding: 15px;">#TRX-${trx.id}</td>
                <td>
                    <div style="font-weight: 600;">${escapeHtml(trx.description)}</div>
                    <div style="font-size: 0.8rem; color: #888;">${typeLabel}: ${escapeHtml(trx.userName)}</div>
                </td>
                <td>${trx.date}</td>
                <td style="${amountClass}">${sign}${formatCurrency(trx.amount)}</td>
                <td>${statusBadge}</td>
            `;
            txBody.appendChild(row);
        });
    }

    // ─── Pending Payout Queue ───────────────────────────────────────────────────

    async function fetchPendingPayouts() {
        try {
            const res = await fetch(PAYOUT_API_URL);
            if (!res.ok) throw new Error('Failed to fetch payout queue');
            const payouts = await res.json();
            renderPayoutQueue(payouts);
        } catch (err) {
            console.error('Error fetching payouts:', err);
            if (payoutBody) payoutBody.innerHTML = '<tr><td colspan="7" style="text-align:center; color:red; padding:20px;">Failed to load payout queue.</td></tr>';
        }
    }

    function renderPayoutQueue(payouts) {
        if (!payoutBody) return;

        // Update count badge
        const badge = document.getElementById('payout-count-badge');
        const countEl = document.getElementById('payout-count');
        if (badge && countEl) {
            if (payouts.length > 0) {
                countEl.textContent = payouts.length;
                badge.style.display = 'inline-flex';
                // Highlight if any are PROCESSING (freelancer requested)
                const hasProcessing = payouts.some(p => p.status === 'PROCESSING');
                badge.className = 'payout-badge' + (hasProcessing ? ' processing' : '');
            } else {
                badge.style.display = 'none';
            }
        }

        if (payouts.length === 0) {
            payoutBody.innerHTML = '<tr><td colspan="7" style="text-align:center; padding: 25px; color: #888;"><i class="fa-solid fa-check-circle" style="color: #4caf50; margin-right: 6px;"></i>All subcontractor payouts are settled.</td></tr>';
            return;
        }

        payoutBody.innerHTML = '';
        payouts.forEach(p => {
            const isProcessing = p.status === 'PROCESSING';
            const row = document.createElement('tr');
            row.style.borderBottom = '1px solid #f0f0f0';
            if (isProcessing) row.style.background = '#fff8f8'; // Highlight requested payouts

            row.innerHTML = `
                <td style="padding: 15px;">
                    <div style="font-weight: 700;">${escapeHtml(p.freelancerName)}</div>
                    <div style="font-size: 0.8rem; color: #888;">${escapeHtml(p.freelancerEmail)}</div>
                </td>
                <td class="bank-detail-cell">
                    <strong>${escapeHtml(p.bankAccountName)}</strong>
                    <span>${escapeHtml(p.bankName)}</span>
                    <span>Branch: ${escapeHtml(p.bankBranchCode)}</span>
                    <span style="font-family: monospace; background: #f5f5f5; padding: 2px 6px; border-radius: 4px;">${escapeHtml(p.bankAccountNumber)}</span>
                </td>
                <td style="font-weight: 800; color: #1a237e; white-space: nowrap;">${formatCurrency(p.amount)}</td>
                <td style="font-size: 0.85rem; max-width: 200px; white-space: normal;">${escapeHtml(p.milestoneDescription)}</td>
                <td style="font-size: 0.85rem; color: #888;">${p.queuedAt}</td>
                <td>
                    ${isProcessing
                        ? '<span class="payout-badge processing" style="font-size:0.75rem;"><i class="fa-solid fa-bell"></i> Requested</span>'
                        : '<span class="payout-badge" style="font-size:0.75rem;"><i class="fa-solid fa-clock"></i> Queued</span>'}
                </td>
                <td>
                    <button class="mark-paid-btn" id="settle-btn-${p.transactionId}" onclick="markAsPaid(${p.transactionId}, this)">
                        <i class="fa-solid fa-check"></i> Mark as Paid
                    </button>
                </td>
            `;
            payoutBody.appendChild(row);
        });
    }

    /**
     * Admin confirms that the manual bank transfer is complete.
     * Calls PUT /api/admin/finance/payouts/{id}/settle
     */
    window.markAsPaid = async function(transactionId, btn) {
        const confirmed = await notify.confirm(
            `Confirm: Have you completed the bank transfer for payout #${transactionId}? This action cannot be undone.`
        );
        if (!confirmed) return;

        btn.disabled = true;
        btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Processing...';

        try {
            const res = await fetch(`${API_CONFIG.BASE_URL}/api/admin/finance/payouts/${transactionId}/settle`, {
                method: 'PUT'
            });

            if (!res.ok) {
                const err = await res.json();
                throw new Error(err.error || 'Failed to settle payout');
            }

            // Remove the row and refresh stats
            const row = btn.closest('tr');
            if (row) row.remove();
            await notify.alert('Payout marked as settled successfully.');
            fetchFinance(); // Refresh stats
            fetchPendingPayouts(); // Refresh queue

        } catch (err) {
            await notify.alert('Error: ' + err.message);
            btn.disabled = false;
            btn.innerHTML = '<i class="fa-solid fa-check"></i> Mark as Paid';
        }
    };

    // ─── Utility Functions ──────────────────────────────────────────────────────

    function getStatusBadge(status) {
        let style = 'background: #eee; color: #666;';
        if (status === 'RECEIVED' || status === 'SETTLED') style = 'background: #e8f5e9; color: #2e7d32;';
        else if (status === 'PENDING') style = 'background: #fff3e0; color: #e65100;';
        else if (status === 'PROCESSING') style = 'background: #fce4ec; color: #c62828;';
        return `<span class="status-badge" style="${style}">${status}</span>`;
    }

    function formatCurrency(value) {
        return new Intl.NumberFormat('en-LK', { style: 'currency', currency: 'LKR' }).format(value || 0);
    }

    function escapeHtml(text) {
        if (!text) return '—';
        return String(text).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;');
    }

    // ─── Initialize ─────────────────────────────────────────────────────────────
    fetchFinance();
    fetchPendingPayouts();
});
