package com.inspeedia.toyotsu.lms.service;

import com.inspeedia.toyotsu.lms.dto.LoginDTO;
import com.inspeedia.toyotsu.lms.model.Admin;
import com.inspeedia.toyotsu.lms.repository.AdminRepository;
import org.springframework.stereotype.Service;

@Service
public class AdminService {

    private final AdminRepository adminRepository;

    public AdminService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public boolean login(LoginDTO loginDTO) {
        return adminRepository.findByUsername(loginDTO.getUsername())
                .map(admin -> admin.getPassword().equals(loginDTO.getPassword()))
                .orElse(false);
    }
}
