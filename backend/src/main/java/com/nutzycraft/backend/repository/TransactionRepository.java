package com.nutzycraft.backend.repository;

import com.nutzycraft.backend.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    
    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = 'CREDIT'")
    Double calculateTotalRevenue();

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.status = 'PENDING' AND t.type = 'DEBIT'")
    Double calculatePendingPayouts();

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.type = 'DEBIT'")
    Double calculateTotalDebits();
}
