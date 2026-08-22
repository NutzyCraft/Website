package com.nutzycraft.backend.repository;

import com.nutzycraft.backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByRelatedUser_EmailOrderByDateDesc(String email);

    List<Transaction> findByFreelancerIdOrderByDateDesc(Long freelancerId);

    /** All money received from clients via PayHere */
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = 'INBOUND_PAYHERE'")
    Double calculateTotalRevenue();

    /** Subcontractor payouts awaiting manual bank transfer (PENDING) */
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = 'OUTBOUND_MANUAL' AND t.status = 'PENDING'")
    Double calculatePendingPayouts();

    /** All outbound amounts to subcontractors (PENDING + SETTLED) */
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = 'OUTBOUND_MANUAL'")
    Double calculateTotalDebits();

    /** Subcontractor payouts that a freelancer has explicitly requested (PROCESSING status) */
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = 'OUTBOUND_MANUAL' AND t.status = 'SETTLED' AND t.relatedUser.email = :email")
    Double calculateTotalSpentByUser(@org.springframework.data.repository.query.Param("email") String email);

    /** Fetch all pending (queued) payouts for the admin finance dashboard */
    List<Transaction> findByTypeAndStatusOrderByDateAsc(Transaction.TransactionType type, Transaction.TransactionStatus status);

    /** Fetch all payouts (any status) for a specific freelancer */
    List<Transaction> findByFreelancerIdAndType(Long freelancerId, Transaction.TransactionType type);
}
