/* NutzyCraft themed notifications — replaces native alert()/confirm() popups. */
(function () {
    const ICONS = {
        success: 'fa-circle-check',
        error: 'fa-circle-exclamation',
        info: 'fa-circle-info',
        warning: 'fa-triangle-exclamation'
    };

    function injectStyles() {
        if (document.getElementById('nutzy-notify-styles')) return;
        const style = document.createElement('style');
        style.id = 'nutzy-notify-styles';
        style.textContent = `
.nutzy-notify-overlay {
    position: fixed;
    inset: 0;
    background: rgba(93, 64, 55, 0.35);
    backdrop-filter: blur(4px);
    -webkit-backdrop-filter: blur(4px);
    display: flex;
    align-items: center;
    justify-content: center;
    z-index: 99999;
    opacity: 0;
    transition: opacity 0.25s ease;
    font-family: var(--font-body, "Quicksand", sans-serif);
}
.nutzy-notify-overlay.nutzy-show { opacity: 1; }
.nutzy-notify-box {
    background: #fffdfb;
    border: 2px solid white;
    border-radius: 24px;
    box-shadow: 0 20px 60px rgba(93, 64, 55, 0.25);
    padding: 28px 30px;
    width: 90%;
    max-width: 380px;
    text-align: center;
    transform: translateY(16px) scale(0.96);
    transition: transform 0.25s cubic-bezier(0.25, 0.8, 0.25, 1);
}
.nutzy-notify-overlay.nutzy-show .nutzy-notify-box { transform: translateY(0) scale(1); }
.nutzy-notify-icon {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 16px;
    font-size: 1.6rem;
    color: white;
}
.nutzy-notify-icon.success { background: var(--accent-green, #b7d9ae); }
.nutzy-notify-icon.error { background: #ff9aa2; }
.nutzy-notify-icon.info { background: var(--secondary, #a8d0e6); }
.nutzy-notify-icon.warning { background: var(--primary, #d4b69d); }
.nutzy-notify-message {
    color: var(--text-main, #5d4037);
    font-size: 1rem;
    line-height: 1.5;
    margin-bottom: 22px;
    white-space: pre-wrap;
    word-break: break-word;
}
.nutzy-notify-actions {
    display: flex;
    gap: 10px;
    justify-content: center;
}
.nutzy-notify-actions button {
    font-family: var(--font-heading, "Nunito", sans-serif);
    font-weight: 700;
    border: none;
    border-radius: 50px;
    padding: 10px 26px;
    font-size: 0.95rem;
    cursor: pointer;
    transition: all 0.2s ease;
}
.nutzy-notify-btn-primary {
    background: var(--primary, #d4b69d);
    color: white;
    box-shadow: 0 4px 15px rgba(212, 182, 157, 0.5);
}
.nutzy-notify-btn-primary:hover { transform: translateY(-2px); box-shadow: 0 8px 20px rgba(212, 182, 157, 0.5); }
.nutzy-notify-btn-secondary {
    background: transparent;
    color: var(--text-muted, #8d6e63);
    border: 2px solid #eee;
}
.nutzy-notify-btn-secondary:hover { border-color: var(--primary, #d4b69d); color: var(--primary, #d4b69d); }
.nutzy-notify-input {
    width: 100%;
    padding: 10px 14px;
    border: 2px solid #eee;
    border-radius: 12px;
    font-family: var(--font-body, "Quicksand", sans-serif);
    font-size: 0.95rem;
    margin-bottom: 20px;
    color: var(--text-main, #5d4037);
    outline: none;
    transition: border-color 0.2s ease;
}
.nutzy-notify-input:focus { border-color: var(--primary, #d4b69d); }
`;
        document.head.appendChild(style);
    }

    function detectType(message) {
        const msg = String(message).toLowerCase();
        if (/(fail|error|denied|invalid|cannot|must|please|required|exceed|not )/.test(msg)) {
            return /please|must|required/.test(msg) ? 'warning' : 'error';
        }
        if (/success|submitted|sent|saved|updated|restored|deleted|completed/.test(msg)) return 'success';
        return 'info';
    }

    function buildOverlay(message, type) {
        injectStyles();
        const overlay = document.createElement('div');
        overlay.className = 'nutzy-notify-overlay';
        overlay.innerHTML = `
            <div class="nutzy-notify-box" role="dialog" aria-modal="true">
                <div class="nutzy-notify-icon ${type}"><i class="fa-solid ${ICONS[type]}"></i></div>
                <div class="nutzy-notify-message"></div>
                <div class="nutzy-notify-actions"></div>
            </div>`;
        overlay.querySelector('.nutzy-notify-message').textContent = message;
        document.body.appendChild(overlay);
        requestAnimationFrame(() => overlay.classList.add('nutzy-show'));
        return overlay;
    }

    function closeOverlay(overlay, callback) {
        overlay.classList.remove('nutzy-show');
        setTimeout(() => {
            overlay.remove();
            if (callback) callback();
        }, 200);
    }

    function nutzyAlert(message, type) {
        return new Promise((resolve) => {
            const overlay = buildOverlay(message, type || detectType(message));
            const actions = overlay.querySelector('.nutzy-notify-actions');
            const okBtn = document.createElement('button');
            okBtn.className = 'nutzy-notify-btn-primary';
            okBtn.textContent = 'OK';
            okBtn.onclick = () => closeOverlay(overlay, resolve);
            actions.appendChild(okBtn);
            okBtn.focus();

            const onKey = (e) => {
                if (e.key === 'Enter' || e.key === 'Escape') {
                    document.removeEventListener('keydown', onKey);
                    closeOverlay(overlay, resolve);
                }
            };
            document.addEventListener('keydown', onKey);
        });
    }

    function nutzyConfirm(message) {
        return new Promise((resolve) => {
            const overlay = buildOverlay(message, 'warning');
            const actions = overlay.querySelector('.nutzy-notify-actions');

            const cancelBtn = document.createElement('button');
            cancelBtn.className = 'nutzy-notify-btn-secondary';
            cancelBtn.textContent = 'Cancel';
            cancelBtn.onclick = () => closeOverlay(overlay, () => resolve(false));

            const okBtn = document.createElement('button');
            okBtn.className = 'nutzy-notify-btn-primary';
            okBtn.textContent = 'Confirm';
            okBtn.onclick = () => closeOverlay(overlay, () => resolve(true));

            actions.appendChild(cancelBtn);
            actions.appendChild(okBtn);
            okBtn.focus();

            const onKey = (e) => {
                if (e.key === 'Escape') {
                    document.removeEventListener('keydown', onKey);
                    closeOverlay(overlay, () => resolve(false));
                } else if (e.key === 'Enter') {
                    document.removeEventListener('keydown', onKey);
                    closeOverlay(overlay, () => resolve(true));
                }
            };
            document.addEventListener('keydown', onKey);
        });
    }

    function nutzyPrompt(message, placeholder) {
        return new Promise((resolve) => {
            const overlay = buildOverlay(message, 'warning');
            const box = overlay.querySelector('.nutzy-notify-box');
            const actions = overlay.querySelector('.nutzy-notify-actions');

            const input = document.createElement('input');
            input.type = 'text';
            input.className = 'nutzy-notify-input';
            if (placeholder) input.placeholder = placeholder;
            box.insertBefore(input, actions);

            const cancelBtn = document.createElement('button');
            cancelBtn.className = 'nutzy-notify-btn-secondary';
            cancelBtn.textContent = 'Cancel';
            cancelBtn.onclick = () => closeOverlay(overlay, () => resolve(null));

            const okBtn = document.createElement('button');
            okBtn.className = 'nutzy-notify-btn-primary';
            okBtn.textContent = 'Confirm';
            okBtn.onclick = () => closeOverlay(overlay, () => resolve(input.value));

            actions.appendChild(cancelBtn);
            actions.appendChild(okBtn);
            input.focus();

            input.addEventListener('keydown', (e) => {
                if (e.key === 'Enter') { e.preventDefault(); okBtn.click(); }
                else if (e.key === 'Escape') { cancelBtn.click(); }
            });
        });
    }

    window.notify = { alert: nutzyAlert, confirm: nutzyConfirm, prompt: nutzyPrompt };

    // Override native alert() app-wide so existing call sites use the themed
    // popup without edits. confirm()/prompt() can't be overridden the same way
    // since they're synchronous natively — call sites use notify.confirm/prompt directly.
    window.alert = function (message) { nutzyAlert(message); };
})();
