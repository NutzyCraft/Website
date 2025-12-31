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

    // Protected Route Interception
    document.addEventListener('click', function (e) {
        // Find the closest anchor tag
        const link = e.target.closest('a');
        if (!link) return;

        const href = link.getAttribute('href');
        if (!href) return;

        // List of protected paths that require login
        // Add more paths here as needed
        // List of protected paths that require login
        // Add more paths here as needed
        const protectedPaths = [
            '/post-job', 'post-job.html', '/post-job.html',
            '/client-dashboard', 'client-dashboard.html', '/client-dashboard.html',
            '/freelancer-dashboard', 'freelancer-dashboard.html', '/freelancer-dashboard.html',
            '/client-settings', 'client-settings.html', '/client-settings.html',
            '/freelancer-my-profile', 'freelancer-my-profile.html', '/freelancer-my-profile.html',
            '/settings', 'settings.html', '/settings.html',
            '/my-proposals', 'my-proposals.html', '/my-proposals.html',
            '/messages', 'messages.html', '/messages.html',
            '/earnings', 'earnings.html', '/earnings.html'
        ];

        // Check if the link is protected
        const isProtected = protectedPaths.some(path => href.includes(path));

        if (isProtected) {
            // Check authentication status
            const isLoggedIn = localStorage.getItem('loggedInEmail') || sessionStorage.getItem('loggedInEmail');

            if (!isLoggedIn) {
                e.preventDefault();
                // Optional: Add a query param to know where to redirect back after registration/login?
                // For now, simple redirect to login as requested
                window.location.href = '/login.html';
            }
        }
    });

    // Global User Profile Sync
    const updateSidebarProfile = async () => {
        const email = localStorage.getItem('loggedInEmail') || sessionStorage.getItem('loggedInEmail');
        if (!email) return;

        try {
            const res = await fetch(`/api/users/profile?email=${email}`);
            if (res.ok) {
                const profile = await res.json();

                // Update Sidebar User Info
                const sidebarUserContainer = document.querySelector('.sidebar-user');
                if (sidebarUserContainer) {
                    const nameEl = sidebarUserContainer.querySelector('div > div:first-child');
                    const roleEl = sidebarUserContainer.querySelector('div > div:last-child');
                    const imgEl = sidebarUserContainer.querySelector('img');

                    if (nameEl) nameEl.textContent = profile.fullName;
                    if (roleEl) {
                        let displayRole = profile.companyName;
                        // Filter out the accidental default value
                        if (displayRole === 'Freelancer Inc') displayRole = null;
                        roleEl.textContent = displayRole || profile.role || 'User';
                    }
                    if (imgEl) {
                        if (profile.profileImage) {
                            imgEl.src = profile.profileImage;
                        } else {
                            imgEl.src = `https://ui-avatars.com/api/?name=${encodeURIComponent(profile.fullName)}&background=D4B69D&color=fff`;
                        }
                    }
                }
            }
        } catch (err) {
            console.error('Failed to sync profile:', err);
        }
    };

    updateSidebarProfile();
});
