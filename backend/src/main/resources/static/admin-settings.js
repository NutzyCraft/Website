document.addEventListener('DOMContentLoaded', () => {
    const API_URL = '/api/admin/settings';
    const form = document.querySelector('form');
    const inputs = {
        siteName: form.querySelector('input[type="text"]'),
        supportEmail: form.querySelector('input[type="email"]'),
        platformFee: form.querySelector('input[type="number"]'),
        maintenanceMode: form.querySelector('select')
    };

    async function loadSettings() {
        try {
            const response = await fetch(API_URL);
            if (!response.ok) throw new Error('Failed to load settings');
            const data = await response.json();

            inputs.siteName.value = data.siteName || '';
            inputs.supportEmail.value = data.supportEmail || '';
            inputs.platformFee.value = data.platformFee || '10';
            inputs.maintenanceMode.value = data.maintenanceMode || 'off';
        } catch (error) {
            console.error('Error loading settings:', error);
        }
    }

    async function saveSettings(e) {
        e.preventDefault();
        const btn = form.querySelector('button');
        const originalText = btn.textContent;
        btn.textContent = 'Saving...';
        btn.disabled = true;

        const payload = {
            siteName: inputs.siteName.value,
            supportEmail: inputs.supportEmail.value,
            platformFee: inputs.platformFee.value,
            maintenanceMode: inputs.maintenanceMode.value
        };

        try {
            const response = await fetch(API_URL, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (!response.ok) throw new Error('Failed to save settings');
            alert('Settings saved successfully!');
        } catch (error) {
            console.error('Error saving settings:', error);
            alert('Failed to save settings.');
        } finally {
            btn.textContent = originalText;
            btn.disabled = false;
        }
    }

    form.addEventListener('submit', saveSettings);
    loadSettings();
});
