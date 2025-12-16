package com.nutzycraft.backend.service;

import com.nutzycraft.backend.dto.AdminJobDTO;
import com.nutzycraft.backend.dto.AdminUserDTO;
import com.nutzycraft.backend.dto.DashboardStatsDTO;
import com.nutzycraft.backend.entity.Job;
import com.nutzycraft.backend.entity.User;
import com.nutzycraft.backend.repository.JobRepository;
import com.nutzycraft.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private com.nutzycraft.backend.repository.TransactionRepository transactionRepository;

    @Autowired
    private com.nutzycraft.backend.repository.DisputeRepository disputeRepository;

    @Autowired
    private com.nutzycraft.backend.repository.SupportMessageRepository supportMessageRepository;

    @Autowired
    private com.nutzycraft.backend.repository.SystemSettingRepository systemSettingRepository;

    @Autowired
    private com.nutzycraft.backend.repository.NotificationRepository notificationRepository;

    public DashboardStatsDTO getDashboardStats() {
        long totalUsers = userRepository.count();
        long activeJobs = jobRepository.countByStatus("IN_PROGRESS");
        Double revenue = jobRepository.sumBudgetByStatus("COMPLETED");
        long openDisputes = 0; // Stubbed for now

        // Fetch recent users (limit 5)
        List<AdminUserDTO> recentUsers = userRepository.findAll(
                org.springframework.data.domain.PageRequest.of(0, 5, org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "id")) // approximate 'joinedAt' by ID desc
        ).stream().map(this::convertToUserDTO).collect(Collectors.toList());

        // Fetch recent activities (notifications)
        List<com.nutzycraft.backend.dto.AdminDTOs.NotificationDTO> recentActivities = getNotifications().stream().limit(10).collect(Collectors.toList());

        return new DashboardStatsDTO(
            totalUsers,
            activeJobs,
            revenue != null ? revenue : 0.0,
            openDisputes,
            recentUsers,
            recentActivities
        );
    }

    public List<AdminUserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::convertToUserDTO)
                .collect(Collectors.toList());
    }

    public List<AdminJobDTO> getAllJobs() {
        return jobRepository.findAll().stream()
                .map(this::convertToJobDTO)
                .collect(Collectors.toList());
    }

    public void updateUserStatus(Long userId, boolean active) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID cannot be null");
        }
        // In a real app we might have an 'active' flag. 
        // For now, we'll just log or maybe toggle verification if that's what we mean.
        // User entity has 'isVerified'. Let's assume active means verified for now or add a field.
        // The DTO has 'active', let's map it to isVerified for simplicity or handle banning differently.
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setVerified(active); // Using verification as a proxy for active status for now
        userRepository.save(user);
    }

    private AdminUserDTO convertToUserDTO(User user) {
        // User entity doesn't have 'joinedAt' or 'active' boolean in the same way.
        // We'll approximate.
        return new AdminUserDTO(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().name() : "UNKNOWN",
                java.time.LocalDateTime.now(), // Placeholder as joinedAt is missing
                user.isVerified()
        );
    }

    private AdminJobDTO convertToJobDTO(Job job) {
        return new AdminJobDTO(
                job.getId(),
                job.getTitle(),
                job.getClient() != null ? job.getClient().getFullName() : "Unknown",
                "Start to Assign", // Placeholder for freelancer
                job.getBudget(),
                job.getStatus()
        );
    }

    public com.nutzycraft.backend.dto.AdminDTOs.AdminFinanceDTO getFinanceStats() {
        Double totalRevenue = transactionRepository.calculateTotalRevenue();
        Double pendingPayouts = transactionRepository.calculatePendingPayouts();
        double revenue = totalRevenue != null ? totalRevenue : 0.0;
        double pending = pendingPayouts != null ? pendingPayouts : 0.0;
        Double totalDebits = transactionRepository.calculateTotalDebits();
        double debits = totalDebits != null ? totalDebits : 0.0;
        
        // Ledger-based Commission: What came in (Revenue) minus what went out (Debits)
        // This accurately represents the "NutzyCraft Bank Account" surplus
        double commission = revenue - debits;

        java.util.List<com.nutzycraft.backend.entity.Transaction> transactions = transactionRepository.findAll(
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "date")
        );
        // Limit to 10 for dashboard/recent view if needed, but here we return all
        
        java.util.List<com.nutzycraft.backend.dto.AdminDTOs.AdminTransactionItemDTO> transactionDTOs = transactions.stream()
                .map(this::convertToTransactionDTO)
                .collect(java.util.stream.Collectors.toList());

        return new com.nutzycraft.backend.dto.AdminDTOs.AdminFinanceDTO(revenue, pending, commission, transactionDTOs);
    }

    public java.util.List<com.nutzycraft.backend.dto.AdminDTOs.AdminDisputeDTO> getAllDisputes() {
        return disputeRepository.findAll().stream()
                .map(this::convertToDisputeDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    public java.util.List<com.nutzycraft.backend.dto.AdminDTOs.AdminSupportDTO> getAllSupportMessages() {
        return supportMessageRepository.findAll().stream()
                .map(this::convertToSupportDTO)
                .collect(java.util.stream.Collectors.toList());
    }

    public com.nutzycraft.backend.dto.AdminDTOs.SystemSettingsDTO getSystemSettings() {
        String siteName = getSettingValue("site_name", "Nutzy Craft");
        String supportEmail = getSettingValue("support_email", "support@nutzy.com");
        String platformFee = getSettingValue("platform_fee", "10");
        String maintenanceMode = getSettingValue("maintenance_mode", "off");
        
        return new com.nutzycraft.backend.dto.AdminDTOs.SystemSettingsDTO(siteName, supportEmail, platformFee, maintenanceMode);
    }

    public void updateSystemSettings(com.nutzycraft.backend.dto.AdminDTOs.SystemSettingsDTO settings) {
        saveSetting("site_name", settings.getSiteName());
        saveSetting("support_email", settings.getSupportEmail());
        saveSetting("platform_fee", settings.getPlatformFee());
        saveSetting("maintenance_mode", settings.getMaintenanceMode());
    }

    public java.util.List<com.nutzycraft.backend.dto.AdminDTOs.NotificationDTO> getNotifications() {
        return notificationRepository.findTop50ByOrderByCreatedAtDesc().stream()
                .map(n -> new com.nutzycraft.backend.dto.AdminDTOs.NotificationDTO(
                        n.getId(), n.getTitle(), n.getMessage(), n.getType(), formatDate(n.getCreatedAt()), n.getLink(), n.isRead()
                ))
                .collect(java.util.stream.Collectors.toList());
    }
    
    public void resolveDispute(Long id) {
        if (id == null) return;
        com.nutzycraft.backend.entity.Dispute d = disputeRepository.findById(id).orElseThrow();
        d.setStatus(com.nutzycraft.backend.entity.Dispute.DisputeStatus.RESOLVED);
        disputeRepository.save(d);
    }

    public void resolveSupportMessage(Long id) {
        if (id == null) return;
        com.nutzycraft.backend.entity.SupportMessage s = supportMessageRepository.findById(id).orElseThrow();
        s.setStatus(com.nutzycraft.backend.entity.SupportMessage.SupportStatus.RESOLVED);
        supportMessageRepository.save(s);
    }

    private String getSettingValue(String key, String defaultValue) {
        if (key == null) return defaultValue;
        return systemSettingRepository.findById(key)
                .map(com.nutzycraft.backend.entity.SystemSetting::getValue)
                .orElse(defaultValue);
    }

    private void saveSetting(String key, String value) {
        com.nutzycraft.backend.entity.SystemSetting setting = new com.nutzycraft.backend.entity.SystemSetting();
        setting.setKey(key);
        setting.setValue(value);
        systemSettingRepository.save(setting);
    }

    private com.nutzycraft.backend.dto.AdminDTOs.AdminTransactionItemDTO convertToTransactionDTO(com.nutzycraft.backend.entity.Transaction t) {
        return new com.nutzycraft.backend.dto.AdminDTOs.AdminTransactionItemDTO(
                t.getId(),
                t.getDescription(),
                t.getRelatedUser() != null ? t.getRelatedUser().getFullName() : "System",
                t.getAmount(),
                t.getStatus().name(),
                formatDate(t.getDate()),
                t.getType().name()
        );
    }

    private com.nutzycraft.backend.dto.AdminDTOs.AdminDisputeDTO convertToDisputeDTO(com.nutzycraft.backend.entity.Dispute d) {
        return new com.nutzycraft.backend.dto.AdminDTOs.AdminDisputeDTO(
                d.getId(),
                d.getClient() != null ? d.getClient().getFullName() : "Unknown",
                d.getFreelancer() != null ? d.getFreelancer().getFullName() : "Unknown",
                d.getIssue(),
                d.getStatus().name(),
                formatDate(d.getCreatedAt())
        );
    }

    private com.nutzycraft.backend.dto.AdminDTOs.AdminSupportDTO convertToSupportDTO(com.nutzycraft.backend.entity.SupportMessage s) {
        return new com.nutzycraft.backend.dto.AdminDTOs.AdminSupportDTO(
                s.getId(),
                s.getSender() != null ? s.getSender().getFullName() : "Guest",
                s.getSender() != null && s.getSender().getRole() != null ? s.getSender().getRole().name() : "GUEST",
                s.getSubject(),
                s.getStatus().name(),
                formatDate(s.getCreatedAt())
        );
    }

    private String formatDate(java.time.LocalDateTime date) {
        if (date == null) return "";
        return date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }
}
