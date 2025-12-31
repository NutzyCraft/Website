document.addEventListener('DOMContentLoaded', () => {
    const API_URL = '/api/admin/support';
    const tableBody = document.querySelector('.dashboard-table tbody');

    async function fetchSupport() {
        try {
            const response = await fetch(API_URL);
            if (!response.ok) throw new Error('Failed to fetch support messages');
            const data = await response.json();
            renderSupport(data);
        } catch (error) {
            console.error('Error fetching support:', error);
            if (tableBody) tableBody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:red;">Failed to load messages.</td></tr>';
        }
    }

    function renderSupport(messages) {
        if (!tableBody) return;
        tableBody.innerHTML = '';

        messages.forEach(msg => {
            const row = document.createElement('tr');
            row.style.borderBottom = '1px solid #f0f0f0';

            const roleClass = msg.role === 'CLIENT' ? 'background: #e3f2fd; color: #1565c0;' : 'background: #e8f5e9; color: #2e7d32;';
            const statusBadge = msg.status === 'OPEN'
                ? '<span class="status-badge" style="background: #ffecb3; color: #ff8f00;">Open</span>'
                : '<span class="status-badge status-active">Resolved</span>';

            row.innerHTML = `
                <td style="padding: 15px;">
                    <div style="display: flex; align-items: center; gap: 10px;">
                        <img src="https://ui-avatars.com/api/?name=${encodeURIComponent(msg.senderName)}&background=random"
                            style="width: 32px; height: 32px; border-radius: 50%;">
                        <div style="font-weight: 600;">${escapeHtml(msg.senderName)}</div>
                    </div>
                </td>
                <td><span class="skill-tag" style="${roleClass}">${msg.role}</span></td>
                <td style="font-weight: 600; color: #333;">${escapeHtml(msg.subject)}</td>
                <td>${msg.date}</td>
                <td>${statusBadge}</td>
                <td>
                    <button onclick="resolveSupport(${msg.id})" class="btn btn-outline" style="padding: 5px 10px; font-size: 0.8rem;"
                     ${msg.status === 'RESOLVED' ? 'disabled' : ''}>
                     ${msg.status === 'RESOLVED' ? 'Done' : 'Resolve'}
                    </button>
                </td>
            `;
            tableBody.appendChild(row);
        });
    }

    window.resolveSupport = async function (id) {
        if (!confirm('Mark this ticket as resolved?')) return;
        try {
            const response = await fetch(`${API_URL}/${id}/resolve`, { method: 'PUT' });
            if (response.ok) {
                fetchSupport(); // Reload list
            } else {
                alert('Failed to update ticket status');
            }
        } catch (e) {
            console.error(e);
            alert('Error updating ticket');
        }
    };

    function escapeHtml(text) {
        if (!text) return '';
        return text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
    }

    fetchSupport();
});
