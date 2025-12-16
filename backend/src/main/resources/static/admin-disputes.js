document.addEventListener('DOMContentLoaded', () => {
    const API_URL = '/api/admin/disputes';
    const tableBody = document.querySelector('.dashboard-table tbody');

    async function fetchDisputes() {
        try {
            const response = await fetch(API_URL);
            if (!response.ok) throw new Error('Failed to fetch disputes');
            const data = await response.json();
            renderDisputes(data);
        } catch (error) {
            console.error('Error fetching disputes:', error);
            if (tableBody) tableBody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:red;">Failed to load disputes.</td></tr>';
        }
    }

    function renderDisputes(disputes) {
        if (!tableBody) return;
        tableBody.innerHTML = '';

        disputes.forEach(d => {
            const row = document.createElement('tr');
            row.style.borderBottom = '1px solid #f0f0f0';

            const statusBadge = d.status === 'OPEN'
                ? '<span class="status-badge status-error">Open</span>'
                : '<span class="status-badge status-active">Resolved</span>';

            row.innerHTML = `
                <td style="padding: 15px;">#CASE-${d.id}</td>
                <td>
                    <div><strong>Client:</strong> ${escapeHtml(d.clientName)}</div>
                    <div><strong>Freelancer:</strong> ${escapeHtml(d.freelancerName)}</div>
                </td>
                <td>${escapeHtml(d.issue)}</td>
                <td>${d.date}</td>
                <td>${statusBadge}</td>
                <td>
                    <button onclick="resolveDispute(${d.id})" class="btn btn-primary"
                        style="padding: 6px 15px; font-size: 0.8rem; background: #1a237e;" 
                        ${d.status === 'RESOLVED' ? 'disabled' : ''}>
                        ${d.status === 'RESOLVED' ? 'Resolved' : 'Mark Resolved'}
                    </button>
                </td>
            `;
            tableBody.appendChild(row);
        });
    }

    window.resolveDispute = async function (id) {
        if (!confirm('Are you sure you want to mark this dispute as resolved?')) return;
        try {
            const response = await fetch(`${API_URL}/${id}/resolve`, { method: 'PUT' });
            if (response.ok) {
                fetchDisputes(); // Reload list
            } else {
                alert('Failed to update dispute status');
            }
        } catch (e) {
            console.error(e);
            alert('Error updating dispute');
        }
    };

    function escapeHtml(text) {
        if (!text) return '';
        return text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
    }

    fetchDisputes();
});
