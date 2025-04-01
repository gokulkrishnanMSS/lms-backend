package com.inspeedia.toyotsu.lms.repository;

import com.inspeedia.toyotsu.lms.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, String> {
    Optional<Admin> findByUsername(String username);
}
