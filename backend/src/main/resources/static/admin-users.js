document.addEventListener('DOMContentLoaded', () => {
    const API_URL = '/api/admin/users';
    const tableBody = document.querySelector('.dashboard-table tbody');

    async function fetchUsers() {
        try {
            const response = await fetch(API_URL);
            if (!response.ok) throw new Error('Failed to fetch users');
            const users = await response.json();
            renderUsers(users);
        } catch (error) {
            console.error('Error fetching users:', error);
            tableBody.innerHTML = '<tr><td colspan="6" style="text-align:center; color:red;">Failed to load users.</td></tr>';
        }
    }

    function renderUsers(users) {
        tableBody.innerHTML = ''; // Clear existing rows

        users.forEach(user => {
            const row = document.createElement('tr');
            row.style.borderBottom = '1px solid #f0f0f0';

            const roleClass = user.role === 'CLIENT' ? 'status-blue' : '';
            const statusBadge = user.active
                ? '<span class="status-badge status-active">Active</span>'
                : '<span class="status-badge" style="background: #eee; color: #666;">Inactive</span>';

            row.innerHTML = `
                <td style="padding: 15px;">#USR-${user.id}</td>
                <td>
                    <div style="display: flex; align-items: center; gap: 10px;">
                        <img src="https://ui-avatars.com/api/?name=${encodeURIComponent(user.name)}&background=random"
                            style="width: 32px; border-radius: 50%;">
                        <div>
                            <div style="font-weight: 600;">${escapeHtml(user.name)}</div>
                            <div style="font-size: 0.8rem; color: #888;">${escapeHtml(user.email)}</div>
                        </div>
                    </div>
                </td>
                <td><span class="skill-tag ${roleClass}">${user.role}</span></td>
                <td>${formatDate(user.joinedAt)}</td>
                <td>${statusBadge}</td>
                <td>
                    <button class="btn btn-outline" style="padding: 4px 10px;">Edit</button>
                    ${renderActionButton(user)}
                </td>
            `;
            tableBody.appendChild(row);
        });
    }

    function renderActionButton(user) {
        if (user.active) {
            return `<button class="btn btn-text" style="color: #d32f2f;" onclick="toggleStatus(${user.id}, false)">Ban</button>`;
        } else {
            return `<button class="btn btn-outline" style="padding: 4px 10px;" onclick="toggleStatus(${user.id}, true)">Unban</button>`;
        }
    }

    window.toggleStatus = async (userId, active) => {
        if (!confirm(`Are you sure you want to ${active ? 'activate' : 'ban'} this user?`)) return;

        try {
            const response = await fetch(`/api/admin/users/${userId}/status?active=${active}`, {
                method: 'PUT'
            });
            if (response.ok) {
                fetchUsers(); // Refresh list
            } else {
                alert('Failed to update status');
            }
        } catch (error) {
            console.error('Error updating status:', error);
            alert('An error occurred');
        }
    };

    function escapeHtml(text) {
        if (!text) return '';
        return text
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    function formatDate(dateString) {
        if (!dateString) return '-';
        const date = new Date(dateString);
        return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
    }

    fetchUsers();
});
