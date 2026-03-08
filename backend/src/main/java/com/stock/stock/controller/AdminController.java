package com.stock.stock.controller;

import com.stock.stock.dto.*;
import com.stock.stock.service.AdminService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = { "http://localhost:3000", "http://localhost:8080", "http://localhost:5173" })
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    // ─── Registration Requests ─────────────────────────────────────────

    @GetMapping("/registration-requests")
    public ResponseEntity<List<RegistrationRequestResponseDto>> getRegistrationRequests(
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(adminService.getRegistrationRequests(status));
    }

    @GetMapping("/generate-credentials/{requestId}")
    public ResponseEntity<GeneratedCredentialsDto> generateCredentials(@PathVariable Long requestId) {
        return ResponseEntity.ok(adminService.generateCredentials(requestId));
    }

    @PutMapping("/registration-requests/{id}/approve")
    public ResponseEntity<?> approveRequest(
            @PathVariable Long id,
            @RequestBody ApproveRequestDto dto) {
        try {
            AdminRegistrationResponseDto response = adminService.approveRequest(id, dto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to approve request {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    @PutMapping("/registration-requests/{id}/reject")
    public ResponseEntity<?> rejectRequest(
            @PathVariable Long id,
            @RequestBody(required = false) RejectRequestDto dto) {
        try {
            RegistrationRequestResponseDto response = adminService.rejectRequest(id, dto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to reject request {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    // ─── Active User Management ────────────────────────────────────────

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(adminService.getAllUsers());
    }

    @PutMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long id,
            @RequestBody RoleUpdateRequest dto) {
        try {
            AdminUserResponseDto response = adminService.updateUserRole(id, dto);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to update user role {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            adminService.deleteUser(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            log.error("Failed to delete user {}: {}", id, e.getMessage(), e);
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "Unknown error"));
        }
    }
}
