package com.nutzycraft.backend.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.nutzycraft.backend.entity.*;
import com.nutzycraft.backend.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDeletionService {

    private final UserRepository userRepository;
    private final FreelancerRepository freelancerRepository;
    private final ClientRepository clientRepository;
    private final JobRepository jobRepository;
    private final ProposalRepository proposalRepository;
    private final NotificationRepository notificationRepository;
    private final ChatRepository chatRepository;
    private final Cloudinary cloudinary;

    /**
     * Soft deletes a user account by marking it as deleted instead of permanently removing it.
     * Associated data like chat messages are also marked as deleted.
     * Admin can permanently delete the account later.
     */
    @Transactional
    public void deleteUserAccount(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Long userId = user.getId();

        log.info("Starting soft delete for user: {} (ID: {})", email, userId);

        // 1. Mark MongoDB chat messages as deleted
        softDeleteChatMessages(userId);

        // 2. Mark the user as deleted
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
        
        log.info("Soft deleted user account: {}", email);
    }

    /**
     * Permanently deletes a user and all their associated data from PostgreSQL, MongoDB, and Cloudinary.
     * This is a destructive operation and cannot be undone.
     * Should only be called by admins for accounts that have been soft-deleted for 30+ days.
     */
    @Transactional
    public void permanentlyDeleteUserAccount(String email) {
        User user = userRepository.findDeletedByEmail(email)
                .orElseThrow(() -> new RuntimeException("Deleted user not found"));

        Long userId = user.getId();
        List<String> cloudinaryUrls = new ArrayList<>();

        log.info("Starting permanent deletion for user: {} (ID: {})", email, userId);

        // 1. Permanently delete MongoDB chat messages
        permanentlyDeleteChatMessages(userId);

        // 2. Collect Cloudinary URLs and delete PostgreSQL data based on role
        if (user.getRole() == User.Role.FREELANCER) {
            cloudinaryUrls.addAll(deleteFreelancerData(email, userId));
        } else if (user.getRole() == User.Role.CLIENT) {
            cloudinaryUrls.addAll(deleteClientData(email, userId));
        }

        // 3. Delete notifications for this user
        deleteNotifications(userId);

        // 4. Delete the user record permanently
        userRepository.delete(user);
        log.info("Permanently deleted user record for: {}", email);

        // 5. Delete Cloudinary assets (after DB transaction commits)
        deleteCloudinaryAssets(cloudinaryUrls);

        log.info("Permanent account deletion completed for user: {}", email);
    }

    private void softDeleteChatMessages(Long userId) {
        try {
            // Mark all messages where user is sender or receiver as deleted
            List<ChatMessage> messages = chatRepository.findBySenderIdOrReceiverIdOrderByTimestampDesc(userId, userId);
            if (!messages.isEmpty()) {
                LocalDateTime now = LocalDateTime.now();
                for (ChatMessage message : messages) {
                    message.setDeletedAt(now);
                }
                chatRepository.saveAll(messages);
                log.info("Soft deleted {} chat messages from MongoDB for user ID: {}", messages.size(), userId);
            }
        } catch (Exception e) {
            log.error("Error soft deleting MongoDB data for user {}: {}", userId, e.getMessage());
            // Continue with deletion even if MongoDB fails
        }
    }

    private void permanentlyDeleteChatMessages(Long userId) {
        try {
            // Permanently delete all messages where user is sender or receiver
            List<ChatMessage> messages = chatRepository.findBySenderIdOrReceiverIdOrderByTimestampDesc(userId, userId);
            if (!messages.isEmpty()) {
                chatRepository.deleteAll(messages);
                log.info("Permanently deleted {} chat messages from MongoDB for user ID: {}", messages.size(), userId);
            }
        } catch (Exception e) {
            log.error("Error permanently deleting MongoDB data for user {}: {}", userId, e.getMessage());
            // Continue with deletion even if MongoDB fails
        }
    }

    private List<String> deleteFreelancerData(String email, Long userId) {
        List<String> cloudinaryUrls = new ArrayList<>();

        // Get freelancer profile
        freelancerRepository.findByUser_Email(email).ifPresent(freelancer -> {
            // Collect Cloudinary URLs from freelancer
            if (freelancer.getProfileImage() != null && freelancer.getProfileImage().contains("cloudinary")) {
                cloudinaryUrls.add(freelancer.getProfileImage());
            }
            if (freelancer.getBannerImage() != null && freelancer.getBannerImage().contains("cloudinary")) {
                cloudinaryUrls.add(freelancer.getBannerImage());
            }

            // Delete proposals by this freelancer
            List<Proposal> proposals = proposalRepository.findByFreelancerEmail(email);
            if (!proposals.isEmpty()) {
                proposalRepository.deleteAll(proposals);
                log.info("Deleted {} proposals for freelancer: {}", proposals.size(), email);
            }

            // Handle jobs where this freelancer is assigned
            // We'll just unassign the freelancer rather than deleting client's jobs
            List<Job> assignedJobs = jobRepository.findByFreelancer_Email(email);
            for (Job job : assignedJobs) {
                job.setFreelancer(null);
                job.setStatus("OPEN"); // Reopen the job so client can find another freelancer
                jobRepository.save(job);
            }
            log.info("Unassigned freelancer from {} jobs", assignedJobs.size());

            // Delete the freelancer profile
            freelancerRepository.delete(freelancer);
            log.info("Deleted freelancer profile for: {}", email);
        });

        return cloudinaryUrls;
    }

    private List<String> deleteClientData(String email, Long userId) {
        List<String> cloudinaryUrls = new ArrayList<>();

        // Get client profile
        clientRepository.findByUser_Email(email).ifPresent(client -> {
            // Collect Cloudinary URLs from client
            if (client.getProfileImage() != null && client.getProfileImage().contains("cloudinary")) {
                cloudinaryUrls.add(client.getProfileImage());
            }

            // Delete jobs created by this client
            List<Job> jobs = jobRepository.findByClient_Email(email);
            for (Job job : jobs) {
                // Collect any Cloudinary attachments from jobs (comma-separated URLs)
                if (job.getAttachments() != null && !job.getAttachments().isEmpty()) {
                    String[] attachmentUrls = job.getAttachments().split(",");
                    for (String attachment : attachmentUrls) {
                        String trimmed = attachment.trim();
                        if (trimmed.contains("cloudinary")) {
                            cloudinaryUrls.add(trimmed);
                        }
                    }
                }
                
                // Delete proposals for this job first
                List<Proposal> proposals = proposalRepository.findByJobId(job.getId());
                if (!proposals.isEmpty()) {
                    proposalRepository.deleteAll(proposals);
                }
            }
            
            // Delete all jobs
            if (!jobs.isEmpty()) {
                jobRepository.deleteAll(jobs);
                log.info("Deleted {} jobs for client: {}", jobs.size(), email);
            }

            // Delete the client profile
            clientRepository.delete(client);
            log.info("Deleted client profile for: {}", email);
        });

        return cloudinaryUrls;
    }

    private void deleteNotifications(Long userId) {
        try {
            List<Notification> notifications = notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
            if (!notifications.isEmpty()) {
                notificationRepository.deleteAll(notifications);
                log.info("Deleted {} notifications for user ID: {}", notifications.size(), userId);
            }
        } catch (Exception e) {
            log.error("Error deleting notifications for user {}: {}", userId, e.getMessage());
        }
    }

    private void deleteCloudinaryAssets(List<String> urls) {
        for (String url : urls) {
            try {
                String publicId = extractPublicId(url);
                if (publicId != null) {
                    cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                    log.info("Deleted Cloudinary asset: {}", publicId);
                }
            } catch (Exception e) {
                log.error("Error deleting Cloudinary asset {}: {}", url, e.getMessage());
                // Continue with other deletions
            }
        }
    }

    /**
     * Extracts the public_id from a Cloudinary URL.
     * Example URL: https://res.cloudinary.com/defxqfmyu/image/upload/v1234567890/sample.jpg
     * Returns: sample (without extension)
     */
    private String extractPublicId(String url) {
        if (url == null || url.isEmpty()) return null;
        
        try {
            // Pattern to match Cloudinary URL and extract public_id
            // Format: .../upload/v{version}/{public_id}.{ext}
            Pattern pattern = Pattern.compile(".*/upload/v\\d+/(.+?)(?:\\.[^.]+)?$");
            Matcher matcher = pattern.matcher(url);
            
            if (matcher.find()) {
                return matcher.group(1);
            }
            
            // Alternative pattern without version
            Pattern pattern2 = Pattern.compile(".*/upload/(.+?)(?:\\.[^.]+)?$");
            Matcher matcher2 = pattern2.matcher(url);
            if (matcher2.find()) {
                return matcher2.group(1);
            }
        } catch (Exception e) {
            log.error("Error extracting public_id from URL {}: {}", url, e.getMessage());
        }
        
        return null;
    }
}
