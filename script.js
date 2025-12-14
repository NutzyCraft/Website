document.addEventListener('DOMContentLoaded', () => {
    // Mobile Menu Toggle
    const mobileBtn = document.querySelector('.mobile-menu-btn');
    const navLinks = document.querySelector('.nav-links');
    const navActions = document.querySelector('.nav-actions');

    if (mobileBtn) {
        mobileBtn.addEventListener('click', () => {
            const isExpanded = mobileBtn.getAttribute('aria-expanded') === 'true';
            mobileBtn.setAttribute('aria-expanded', !isExpanded);

            // Simple toggle for now, could be animated class
            navLinks.style.display = navLinks.style.display === 'flex' ? '' : 'flex';
            navLinks.style.flexDirection = 'column';
            navLinks.style.position = 'absolute';
            navLinks.style.top = '70px';
            navLinks.style.left = '0';
            navLinks.style.width = '100%';
            navLinks.style.background = '#0f0f13';
            navLinks.style.padding = '20px';
            navLinks.style.border = '1px solid rgba(255,255,255,0.1)';

            navActions.style.display = navActions.style.display === 'flex' ? '' : 'flex';
            navActions.style.flexDirection = 'column';
            // Note: This is a basic toggle, a CSS class based approach is better for production
        });
    }

    // Intersection Observer for Scroll Animations
    const observerOptions = {
        threshold: 0.1,
        rootMargin: '0px 0px -50px 0px'
    };

    const observer = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
            if (entry.isIntersecting) {
                entry.target.classList.add('visible');
                observer.unobserve(entry.target);
            }
        });
    }, observerOptions);

    // Elements to animate
    const animatedElements = document.querySelectorAll('.service-card, .step-item, .cta-box, .section-title');

    animatedElements.forEach((el, index) => {
        el.style.opacity = '0';
        el.style.transform = 'translateY(20px)';
        el.style.transition = 'opacity 0.6s ease, transform 0.6s ease';
        if (el.classList.contains('service-card')) {
            el.style.transitionDelay = `${index * 0.1}s`; // Stagger effect
        }
        observer.observe(el);
    });

    // Add visible class styles dynamically or rely on inline styles being overridden
    // Ideally, we add a class '.fade-in-up' and handle it in CSS.
    // Let's inject a style for .visible
    const style = document.createElement('style');
    style.innerHTML = `
        .visible {
            opacity: 1 !important;
            transform: translateY(0) !important;
        }
    `;
    document.head.appendChild(style);

    // Dashboard Sidebar Toggle
    const sidebarToggleBtn = document.querySelector('.sidebar-toggle-btn');
    const dashboardSidebar = document.querySelector('.dashboard-sidebar');

    if (sidebarToggleBtn && dashboardSidebar) {
        sidebarToggleBtn.addEventListener('click', () => {
            dashboardSidebar.classList.toggle('active');

            // Create overlay if not exists
            if (dashboardSidebar.classList.contains('active')) {
                let overlay = document.querySelector('.sidebar-overlay');
                if (!overlay) {
                    overlay = document.createElement('div');
                    overlay.className = 'sidebar-overlay';
                    overlay.style.position = 'fixed';
                    overlay.style.top = '0';
                    overlay.style.left = '0';
                    overlay.style.width = '100vw';
                    overlay.style.height = '100vh';
                    overlay.style.background = 'rgba(0,0,0,0.5)';
                    overlay.style.zIndex = '99';
                    document.body.appendChild(overlay);

                    overlay.addEventListener('click', () => {
                        dashboardSidebar.classList.remove('active');
                        overlay.remove();
                    });
                }
            }
        });
    }

    // Smooth Scroll for Anchor Links (with offset for fixed header)
    document.querySelectorAll('a[href^="#"]').forEach(anchor => {
        anchor.addEventListener('click', function (e) {
            e.preventDefault();
            const targetId = this.getAttribute('href');
            if (targetId === '#') return;

            const targetElement = document.querySelector(targetId);
            if (targetElement) {
                const headerOffset = 100;
                const elementPosition = targetElement.getBoundingClientRect().top;
                const offsetPosition = elementPosition + window.pageYOffset - headerOffset;

                window.scrollTo({
                    top: offsetPosition,
                    behavior: "smooth"
                });
            }
        });
    });
    // Image Preview Logic for Profile Page & Settings
    const handleImagePreview = (inputId, imgId, backgroundTargetId = null) => {
        const input = document.getElementById(inputId);
        if (!input) return;

        input.addEventListener('change', function () {
            if (this.files && this.files[0]) {
                const reader = new FileReader();
                reader.onload = function (e) {
                    if (imgId) {
                        const img = document.getElementById(imgId);
                        if (img) {
                            img.src = e.target.result;
                            img.style.display = 'block'; // Ensure visible (for cover placeholder)
                        }
                    }
                    if (backgroundTargetId) {
                        const bgElement = document.getElementById(backgroundTargetId);
                        if (bgElement) {
                            bgElement.style.backgroundImage = `url('${e.target.result}')`;
                            bgElement.style.backgroundSize = 'cover';
                            bgElement.style.backgroundPosition = 'center';
                        }
                    }

                    // Hide placeholder if exists
                    if (inputId === 'settings-cover-upload') {
                        const placeholder = document.getElementById('settings-cover-placeholder');
                        if (placeholder) placeholder.style.display = 'none';
                    }
                }
                reader.readAsDataURL(this.files[0]);
            }
        });
    };

    // Initialize for My Profile Page
    handleImagePreview('avatar-upload', 'profile-avatar');
    handleImagePreview('banner-upload', null, 'banner-container');

    // Initialize for Settings Page
    handleImagePreview('settings-avatar-upload', 'settings-avatar-preview');
    handleImagePreview('settings-cover-upload', 'settings-cover-preview');

});
