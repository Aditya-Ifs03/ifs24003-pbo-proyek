package org.delcom.app.repositories;

import org.delcom.app.entities.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    // Menampilkan data hanya milik user yang login
    List<Customer> findByUserId(UUID userId);
    
    // Untuk Chart: Menghitung jumlah customer berdasarkan tipe (VIP/Regular) per User
    @Query("SELECT c.type, COUNT(c) FROM Customer c WHERE c.userId = ?1 GROUP BY c.type")
    List<Object[]> countCustomerByType(UUID userId);
}