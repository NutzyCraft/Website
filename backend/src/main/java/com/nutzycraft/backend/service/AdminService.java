package com.nutzycraft.backend.service;

import com.nutzycraft.backend.dto.AdminJobDTO;
import com.nutzycraft.backend.dto.AdminUserDTO;
import com.nutzycraft.backend.dto.DashboardStatsDTO;
import com.nutzycraft.backend.entity.Job;
import com.nutzycraft.backend.entity.Freelancer;
import com.nutzycraft.backend.entity.Transaction;
import com.nutzycraft.backend.entity.User;
import com.nutzycraft.backend.repository.FreelancerRepository;
import com.nutzycraft.backend.repository.JobRepository;
import com.nutzycraft.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private FreelancerRepository freelancerRepository;
    
    @Autowired
    private UserDeletionService userDeletionService;

    public DashboardStatsDTO getDashboardStats() {
        long totalUsers = userRepository.count();
        long activeJobs = jobRepository.countByStatus("IN_PROGRESS");
        Double revenue = jobRepository.sumBudgetByStatus("COMPLETED");
        long openDisputes = 0; // Stubbed for now

        // Fetch recent users (limit 5)
        List<AdminUserDTO> recentUsers = userRepository.findAll(
                org.springframework.data.domain.PageRequest.of(0, 5, org.springframework.data.domain.Sort
                        .by(org.springframework.data.domain.Sort.Direction.DESC, "id")) // approximate 'joinedAt' by ID
                                                                                        // desc
        ).stream().map(this::convertToUserDTO).collect(Collectors.toList());

        // Fetch recent activities (notifications)
        List<com.nutzycraft.backend.dto.AdminDTOs.NotificationDTO> recentActivities = getNotifications().stream()
                .limit(10).collect(Collectors.toList());

        return new DashboardStatsDTO(
                totalUsers,
                activeJobs,
                revenue != null ? revenue : 0.0,
                openDisputes,
                recentUsers,
                recentActivities);
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
        // User entity has 'isVerified'. Let's assume active means verified for now or
        // add a field.
        // The DTO has 'active', let's map it to isVerified for simplicity or handle
        // banning differently.
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        // TODO: Add an 'active' boolean field to User entity if user banning is needed.
        // For now, this is a no-op since isVerified was removed (verification is handled by Clerk).
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
                true); // All Clerk-verified users are considered active
    }

    private AdminJobDTO convertToJobDTO(Job job) {
        return new AdminJobDTO(
                job.getId(),
                job.getTitle(),
                job.getClient() != null ? job.getClient().getDisplayName() : "Unknown",
                job.getFreelancer() != null ? job.getFreelancer().getDisplayName() : "Not Assigned",
                job.getBudget(),
                job.getStatus(),
                job.getClient() != null ? job.getClient().getId() : null,
                job.getFreelancer() != null ? job.getFreelancer().getId() : null);
    }

    public com.nutzycraft.backend.dto.AdminDTOs.AdminFinanceDTO getFinanceStats() {
        Double totalRevenue = transactionRepository.calculateTotalRevenue();
        Double pendingPayouts = transactionRepository.calculatePendingPayouts();
        double revenue = totalRevenue != null ? totalRevenue : 0.0;
        double pending = pendingPayouts != null ? pendingPayouts : 0.0;
        Double totalDebits = transactionRepository.calculateTotalDebits();
        double debits = totalDebits != null ? totalDebits : 0.0;

        // Net Margin: money received from clients minus what we owe subcontractors
        double commission = revenue - debits;

        java.util.List<com.nutzycraft.backend.entity.Transaction> transactions = transactionRepository.findAll(
                org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "date"));

        java.util.List<com.nutzycraft.backend.dto.AdminDTOs.AdminTransactionItemDTO> transactionDTOs = transactions
                .stream()
                .map(this::convertToTransactionDTO)
                .collect(java.util.stream.Collectors.toList());

        // Build pending payout queue
        java.util.List<com.nutzycraft.backend.dto.AdminDTOs.PendingPayoutDTO> payoutQueue = getPendingPayouts();

        return new com.nutzycraft.backend.dto.AdminDTOs.AdminFinanceDTO(revenue, pending, commission, transactionDTOs, payoutQueue);
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

        return new com.nutzycraft.backend.dto.AdminDTOs.SystemSettingsDTO(siteName, supportEmail, platformFee,
                maintenanceMode);
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
                        n.getId(), n.getTitle(), n.getMessage(), n.getType(), formatDate(n.getCreatedAt()), n.getLink(),
                        n.isRead()))
                .collect(java.util.stream.Collectors.toList());
    }

    public void resolveDispute(Long id) {
        if (id == null)
            return;
        com.nutzycraft.backend.entity.Dispute d = disputeRepository.findById(id).orElseThrow();
        d.setStatus(com.nutzycraft.backend.entity.Dispute.DisputeStatus.RESOLVED);
        disputeRepository.save(d);
    }

    public void resolveSupportMessage(Long id) {
        if (id == null)
            return;
        com.nutzycraft.backend.entity.SupportMessage s = supportMessageRepository.findById(id).orElseThrow();
        s.setStatus(com.nutzycraft.backend.entity.SupportMessage.SupportStatus.RESOLVED);
        supportMessageRepository.save(s);
    }

    private String getSettingValue(String key, String defaultValue) {
        if (key == null)
            return defaultValue;
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

    private com.nutzycraft.backend.dto.AdminDTOs.AdminTransactionItemDTO convertToTransactionDTO(
            com.nutzycraft.backend.entity.Transaction t) {
        return new com.nutzycraft.backend.dto.AdminDTOs.AdminTransactionItemDTO(
                t.getId(),
                t.getDescription(),
                t.getRelatedUser() != null ? t.getRelatedUser().getDisplayName() : "System",
                t.getAmount(),
                t.getStatus().name(),
                formatDate(t.getDate()),
                t.getType().name()); // INBOUND_PAYHERE or OUTBOUND_MANUAL
    }

    /**
     * Returns all pending (PENDING or PROCESSING) subcontractor payouts for the admin queue.
     * Enriches each with the freelancer's bank account details.
     */
    public java.util.List<com.nutzycraft.backend.dto.AdminDTOs.PendingPayoutDTO> getPendingPayouts() {
        // Fetch PENDING payouts
        java.util.List<com.nutzycraft.backend.entity.Transaction> pending =
                transactionRepository.findByTypeAndStatusOrderByDateAsc(
                        com.nutzycraft.backend.entity.Transaction.TransactionType.OUTBOUND_MANUAL,
                        com.nutzycraft.backend.entity.Transaction.TransactionStatus.PENDING);
        // Fetch PROCESSING payouts (freelancer requested payout)
        java.util.List<com.nutzycraft.backend.entity.Transaction> processing =
                transactionRepository.findByTypeAndStatusOrderByDateAsc(
                        com.nutzycraft.backend.entity.Transaction.TransactionType.OUTBOUND_MANUAL,
                        com.nutzycraft.backend.entity.Transaction.TransactionStatus.PROCESSING);

        java.util.List<com.nutzycraft.backend.entity.Transaction> allQueued = new java.util.ArrayList<>();
        allQueued.addAll(processing); // Show PROCESSING first (freelancer requested)
        allQueued.addAll(pending);

        return allQueued.stream().map(t -> {
            Freelancer fl = t.getFreelancerId() != null
                    ? freelancerRepository.findById(t.getFreelancerId()).orElse(null)
                    : null;

            String bankName = fl != null && fl.getBankName() != null ? fl.getBankName() : "Not Provided";
            String branchCode = fl != null && fl.getBankBranchCode() != null ? fl.getBankBranchCode() : "—";
            String accountName = fl != null && fl.getBankAccountName() != null ? fl.getBankAccountName() : "Not Provided";
            String accountNumber = fl != null && fl.getBankAccountNumber() != null ? fl.getBankAccountNumber() : "Not Provided";

            String freelancerName = t.getRelatedUser() != null ? t.getRelatedUser().getDisplayName() : "Unknown";
            String freelancerEmail = t.getRelatedUser() != null ? t.getRelatedUser().getEmail() : "";

            return new com.nutzycraft.backend.dto.AdminDTOs.PendingPayoutDTO(
                    t.getId(), freelancerName, freelancerEmail,
                    bankName, branchCode, accountName, accountNumber,
                    t.getAmount(), "LKR",
                    t.getDescription(),
                    formatDate(t.getDate()),
                    t.getStatus().name());
        }).collect(java.util.stream.Collectors.toList());
    }

    /**
     * Admin marks a manual bank transfer as complete, transitioning the payout
     * transaction from PENDING/PROCESSING → SETTLED.
     */
    @Transactional
    public void markPayoutSettled(Long transactionId) {
        if (transactionId == null) throw new IllegalArgumentException("Transaction ID cannot be null");
        com.nutzycraft.backend.entity.Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found: " + transactionId));

        if (tx.getType() != com.nutzycraft.backend.entity.Transaction.TransactionType.OUTBOUND_MANUAL) {
            throw new RuntimeException("Only OUTBOUND_MANUAL transactions can be settled");
        }

        tx.setStatus(com.nutzycraft.backend.entity.Transaction.TransactionStatus.SETTLED);
        transactionRepository.save(tx);
    }

    /**
     * Called when a freelancer clicks "Request Payout" on their earnings page.
     * Transitions the payout from PENDING → PROCESSING so admin knows action is needed.
     */
    @Transactional
    public void createPayoutRequest(Long transactionId, String freelancerEmail) {
        com.nutzycraft.backend.entity.Transaction tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Transaction not found"));

        if (tx.getStatus() != com.nutzycraft.backend.entity.Transaction.TransactionStatus.PENDING) {
            throw new RuntimeException("Only PENDING payouts can be requested");
        }

        // Security: ensure the freelancer requesting payout owns this transaction
        if (tx.getRelatedUser() == null || !tx.getRelatedUser().getEmail().equals(freelancerEmail)) {
            throw new RuntimeException("Unauthorized payout request");
        }

        tx.setStatus(com.nutzycraft.backend.entity.Transaction.TransactionStatus.PROCESSING);
        transactionRepository.save(tx);
    }

    private com.nutzycraft.backend.dto.AdminDTOs.AdminDisputeDTO convertToDisputeDTO(
            com.nutzycraft.backend.entity.Dispute d) {
        return new com.nutzycraft.backend.dto.AdminDTOs.AdminDisputeDTO(
                d.getId(),
                d.getClient() != null ? d.getClient().getDisplayName() : "Unknown",
                d.getFreelancer() != null ? d.getFreelancer().getDisplayName() : "Unknown",
                d.getIssue(),
                d.getStatus().name(),
                formatDate(d.getCreatedAt()));
    }

    private com.nutzycraft.backend.dto.AdminDTOs.AdminSupportDTO convertToSupportDTO(
            com.nutzycraft.backend.entity.SupportMessage s) {
        return new com.nutzycraft.backend.dto.AdminDTOs.AdminSupportDTO(
                s.getId(),
                s.getSender() != null ? s.getSender().getDisplayName() : "Guest",
                s.getSender() != null && s.getSender().getRole() != null ? s.getSender().getRole().name() : "GUEST",
                s.getSubject(),
                s.getStatus().name(),
                formatDate(s.getCreatedAt()));
    }

    private String formatDate(java.time.LocalDateTime date) {
        if (date == null)
            return "";
        return date.format(java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }
    
    /**
     * Get all soft-deleted users with details
     */
    public List<Map<String, Object>> getDeletedUsers() {
        List<User> deletedUsers = userRepository.findAllDeleted();
        LocalDateTime now = LocalDateTime.now();
        
        return deletedUsers.stream().map(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("email", user.getEmail());
            userMap.put("fullName", user.getFullName());
            userMap.put("role", user.getRole().name());
            userMap.put("deletedAt", user.getDeletedAt());
            
            if (user.getDeletedAt() != null) {
                long daysSinceDeletion = ChronoUnit.DAYS.between(user.getDeletedAt(), now);
                userMap.put("daysSinceDeletion", daysSinceDeletion);
            } else {
                userMap.put("daysSinceDeletion", 0);
            }
            
            return userMap;
        }).collect(Collectors.toList());
    }
    
    /**
     * Restore a soft-deleted user account
     */
    @Transactional
    public void restoreUser(@NonNull Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        if (!user.isDeleted()) {
            throw new RuntimeException("User is not deleted");
        }
        
        user.setDeleted(false);
        user.setDeletedAt(null);
        userRepository.save(user);
    }
    
    /**
     * Permanently delete a user account
     */
    @Transactional
    public void permanentlyDeleteUser(@NonNull Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
                
        if (!user.isDeleted()) {
            throw new RuntimeException("User must be soft-deleted first");
        }
        
        // Call the permanent deletion service
        userDeletionService.permanentlyDeleteUserAccount(user.getEmail());
    }
}
