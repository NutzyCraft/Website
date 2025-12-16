document.addEventListener('DOMContentLoaded', () => {
    const API_URL = '/api/admin/notifications';
    const listContainer = document.querySelector('.notification-list');

    async function fetchNotifications() {
        try {
            const response = await fetch(API_URL);
            if (!response.ok) throw new Error('Failed to fetch notifications');
            const data = await response.json();
            renderNotifications(data);
        } catch (error) {
            console.error('Error fetching notifications:', error);
            if (listContainer) listContainer.innerHTML = '<div style="padding: 20px; text-align: center; color: red;">Failed to load notifications.</div>';
        }
    }

    function renderNotifications(notifications) {
        if (!listContainer) return;
        listContainer.innerHTML = '';

        if (notifications.length === 0) {
            listContainer.innerHTML = '<div style="padding: 20px; text-align: center; color: #666;">No notifications.</div>';
            return;
        }

        notifications.forEach(n => {
            const item = document.createElement('div');
            item.className = 'notification-item' + (n.read ? '' : ' unread');
            item.style.cssText = `padding: 20px; border-bottom: 1px solid #f0f0f0; display: flex; gap: 15px; align-items: start; ${n.read ? '' : 'background: #FFF9F5;'}`;

            let iconHtml = '';
            if (n.type === 'ERROR') {
                iconHtml = `<div style="background: #FFEBEE; color: #D32F2F; width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0;"><i class="fa-solid fa-triangle-exclamation"></i></div>`;
            } else if (n.type === 'INFO') {
                iconHtml = `<div style="background: #E8F5E9; color: #2E7D32; width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0;"><i class="fa-solid fa-server"></i></div>`;
            } else {
                iconHtml = `<div style="background: #E3F2FD; color: #1565C0; width: 40px; height: 40px; border-radius: 50%; display: flex; align-items: center; justify-content: center; flex-shrink: 0;"><i class="fa-solid fa-bell"></i></div>`;
            }

            const linkHtml = n.link ?
                `<div style="margin-top: 10px;">
                    <a href="${n.link}" class="btn btn-outline" style="padding: 6px 15px; font-size: 0.8rem; text-decoration: none;">View Details</a>
                 </div>` : '';

            item.innerHTML = `
                ${iconHtml}
                <div style="flex: 1;">
                    <div style="font-weight: 700; font-size: 0.95rem; margin-bottom: 5px;">${escapeHtml(n.title)}
                        ${!n.read ? '<span style="background: #D32F2F; width: 8px; height: 8px; border-radius: 50%; display: inline-block; margin-left: 5px;"></span>' : ''}
                    </div>
                    <p style="font-size: 0.9rem; color: #666; margin-bottom: 5px;">${escapeHtml(n.message)}</p>
                    <div style="font-size: 0.8rem; color: #999;">${n.date}</div>
                    ${linkHtml}
                </div>
            `;
            listContainer.appendChild(item);
        });
    }

    function escapeHtml(text) {
        if (!text) return '';
        return text.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
    }

    fetchNotifications();
});
