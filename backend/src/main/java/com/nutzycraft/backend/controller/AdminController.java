package com.nutzycraft.backend.controller;

import com.nutzycraft.backend.dto.AdminJobDTO;
import com.nutzycraft.backend.dto.AdminUserDTO;
import com.nutzycraft.backend.dto.DashboardStatsDTO;
import com.nutzycraft.backend.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*") // Allow frontend access
public class AdminController {

    @Autowired
    private AdminService adminService;

    @GetMapping("/dashboard/stats")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserDTO>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<Void> updateUserStatus(@PathVariable Long id, @RequestParam boolean active) {
        adminService.updateUserStatus(id, active);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<AdminJobDTO>> getAllJobs() {
        return ResponseEntity.ok(adminService.getAllJobs());
    }

    @GetMapping("/finance")
    public ResponseEntity<com.nutzycraft.backend.dto.AdminDTOs.AdminFinanceDTO> getFinanceStats() {
        return ResponseEntity.ok(adminService.getFinanceStats());
    }

    @GetMapping("/disputes")
    public ResponseEntity<List<com.nutzycraft.backend.dto.AdminDTOs.AdminDisputeDTO>> getAllDisputes() {
        return ResponseEntity.ok(adminService.getAllDisputes());
    }

    @GetMapping("/support")
    public ResponseEntity<List<com.nutzycraft.backend.dto.AdminDTOs.AdminSupportDTO>> getAllSupportMessages() {
        return ResponseEntity.ok(adminService.getAllSupportMessages());
    }

    @GetMapping("/settings")
    public ResponseEntity<com.nutzycraft.backend.dto.AdminDTOs.SystemSettingsDTO> getSettings() {
        return ResponseEntity.ok(adminService.getSystemSettings());
    }

    @PostMapping("/settings")
    public ResponseEntity<Void> updateSettings(@RequestBody com.nutzycraft.backend.dto.AdminDTOs.SystemSettingsDTO settings) {
        adminService.updateSystemSettings(settings);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<com.nutzycraft.backend.dto.AdminDTOs.NotificationDTO>> getNotifications() {
        return ResponseEntity.ok(adminService.getNotifications());
    }
    
    @PutMapping("/disputes/{id}/resolve")
    public ResponseEntity<Void> resolveDispute(@PathVariable Long id) {
        adminService.resolveDispute(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/support/{id}/resolve")
    public ResponseEntity<Void> resolveSupport(@PathVariable Long id) {
        adminService.resolveSupportMessage(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/finance/export")
    public void exportFinanceReport(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=finance_report.csv");
        
        com.nutzycraft.backend.dto.AdminDTOs.AdminFinanceDTO stats = adminService.getFinanceStats();
        java.io.PrintWriter writer = response.getWriter();
        
        // Write CSV Header
        writer.println("Transaction ID,Description,User,Type,Amount,Status,Date");
        
        // Write Data
        for (com.nutzycraft.backend.dto.AdminDTOs.AdminTransactionItemDTO trx : stats.getRecentTransactions()) {
             writer.printf("%d,\"%s\",\"%s\",%s,%.2f,%s,%s%n",
                 trx.getId(),
                 trx.getDescription().replace("\"", "\"\""), // Escape quotes
                 trx.getUserName(),
                 trx.getType(),
                 trx.getAmount(),
                 trx.getStatus(),
                 trx.getDate()
             );
        }
    }
}
