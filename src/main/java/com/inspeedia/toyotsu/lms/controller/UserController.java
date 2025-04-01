package com.inspeedia.toyotsu.lms.controller;

import com.inspeedia.toyotsu.lms.dto.LoginDTO;
import com.inspeedia.toyotsu.lms.service.AdminService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class UserController {
    private final Logger log = LoggerFactory.getLogger(UserController.class);
    private final AdminService adminService;

    @SuppressWarnings("unused")
    public UserController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping("/login")
    @SuppressWarnings("unused")
    public ResponseEntity<String> login(@RequestBody LoginDTO loginDTO) {
        log.info("Login request is received");
        return adminService.login(loginDTO)
                ? ResponseEntity.status(HttpStatus.OK).body("login_success")
                : ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("bad_credentials");
    }
    @GetMapping("/authenticate")
    public ResponseEntity<Boolean> getAuthenticated(){
        return ResponseEntity.ok().body(true);
    }
}
