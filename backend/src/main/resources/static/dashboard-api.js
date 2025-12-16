document.addEventListener('DOMContentLoaded', () => {
    const API_URL = '/api/admin/dashboard/stats';

    async function fetchDashboardStats() {
        try {
            const response = await fetch(API_URL);
            if (!response.ok) {
                throw new Error('Network response was not ok');
            }
            const data = await response.json();

            // Update DOM
            updateStat('stat-total-users', data.totalUsers);
            updateStat('stat-active-jobs', data.activeJobs);
            updateStat('stat-revenue', formatCurrency(data.revenue));
            updateStat('stat-disputes', data.openDisputes);

            if (data.recentUsers) renderRecentUsers(data.recentUsers);
            if (data.recentActivities) renderActivityFeed(data.recentActivities);

        } catch (error) {
            console.error('Failed to fetch dashboard stats:', error);
        }
    }

    function renderRecentUsers(users) {
        const tbody = document.getElementById('new-registrations-body');
        if (!tbody) return;
        tbody.innerHTML = '';
        users.forEach(user => {
            const row = document.createElement('tr');
            row.style.borderBottom = '1px solid #f0f0f0';
            row.innerHTML = `
                <td style="padding: 15px;">
                    <div style="display: flex; align-items: center; gap: 10px;">
                        <img src="https://ui-avatars.com/api/?name=${encodeURIComponent(user.fullName)}&background=random" style="width: 32px; height: 32px; border-radius: 50%;">
                        <div>
                            <div style="font-weight: 600;">${escapeHtml(user.fullName)}</div>
                            <div style="font-size: 0.8rem; color: #888;">${escapeHtml(user.email)}</div>
                        </div>
                    </div>
                </td>
                <td><span class="skill-tag" style="background: #e3f2fd; color: #1565c0;">${user.role}</span></td>
                <td>${user.joinedAt ? new Date(user.joinedAt).toLocaleDateString() : 'N/A'}</td>
                <td><span class="status-badge status-active">Active</span></td>
            `;
            tbody.appendChild(row);
        });
    }

    function renderActivityFeed(activities) {
        const feed = document.getElementById('activity-feed');
        if (!feed) return;
        feed.innerHTML = '';
        if (activities.length === 0) {
            feed.innerHTML = '<div style="padding: 20px; text-align: center; color: #666;">No recent activity.</div>';
            return;
        }
        activities.forEach(act => {
            const item = document.createElement('div');
            item.style.cssText = 'padding: 15px; border-bottom: 1px solid #f0f0f0; display: flex; gap: 10px; align-items: center;';
            let icon = '<i class="fa-solid fa-bell" style="color: #1565C0;"></i>';
            if (act.type === 'Info') icon = '<i class="fa-solid fa-info-circle" style="color: #2E7D32;"></i>';
            if (act.type === 'Alert') icon = '<i class="fa-solid fa-triangle-exclamation" style="color: #D32F2F;"></i>';

            item.innerHTML = `
                <div style="width: 30px; height: 30px; background: #f5f5f5; border-radius: 50%; display: flex; align-items: center; justify-content: center;">${icon}</div>
                <div style="flex: 1;">
                    <div style="font-size: 0.9rem; font-weight: 600;">${escapeHtml(act.title)}</div>
                    <div style="font-size: 0.8rem; color: #666;">${escapeHtml(act.message)}</div>
                </div>
                <div style="font-size: 0.75rem; color: #999;">${act.date}</div>
             `;
            feed.appendChild(item);
        });
    }

    function escapeHtml(text) {
        if (!text) return '';
        return text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
    }

    function updateStat(elementId, value) {
        const el = document.getElementById(elementId);
        if (el) {
            el.textContent = value;
            // formatted value animation could go here
        }
    }

    function formatCurrency(value) {
        return new Intl.NumberFormat('en-US', {
            style: 'currency',
            currency: 'USD'
        }).format(value);
    }

    fetchDashboardStats();
});
