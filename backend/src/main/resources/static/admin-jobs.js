document.addEventListener('DOMContentLoaded', () => {
    const API_URL = '/api/admin/jobs';
    const tableBody = document.querySelector('.dashboard-table tbody');

    async function fetchJobs() {
        try {
            const response = await fetch(API_URL);
            if (!response.ok) throw new Error('Failed to fetch jobs');
            const jobs = await response.json();
            renderJobs(jobs);
        } catch (error) {
            console.error('Error fetching jobs:', error);
            tableBody.innerHTML = '<tr><td colspan="7" style="text-align:center; color:red;">Failed to load jobs.</td></tr>';
        }
    }

    function renderJobs(jobs) {
        tableBody.innerHTML = ''; // Clear existing rows

        jobs.forEach(job => {
            const row = document.createElement('tr');
            row.style.borderBottom = '1px solid #f0f0f0';

            const statusBadge = getStatusBadge(job.status);
            const value = new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(job.value);

            row.innerHTML = `
                <td style="padding: 15px;">#JOB-${job.id}</td>
                <td style="font-weight: 600;">${escapeHtml(job.title)}</td>
                <td>${escapeHtml(job.clientName)}</td>
                <td>${escapeHtml(job.freelancerName)}</td>
                <td>${value}</td>
                <td>${statusBadge}</td>
                <td style="text-align: center;">
                    <a href="#" class="btn btn-outline"
                        style="padding: 6px 12px; font-size: 0.85rem; text-decoration: none; display: inline-block;">View Tracking</a>
                </td>
            `;
            tableBody.appendChild(row);
        });
    }

    function getStatusBadge(status) {
        // Mock status styling
        let style = 'background: #eee; color: #666;';
        if (status === 'COMPLETED') style = 'background: #e8f5e9; color: #2e7d32;';
        else if (status === 'IN_PROGRESS') style = 'background: #e3f2fd; color: #1565c0;'; // Fixed status-active class issue
        else if (status === 'HIRING') style = 'background: #fff3e0; color: #e65100;';

        return `<span class="status-badge" style="${style}">${status}</span>`;
    }

    function escapeHtml(text) {
        if (!text) return '';
        return text
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    fetchJobs();
});
