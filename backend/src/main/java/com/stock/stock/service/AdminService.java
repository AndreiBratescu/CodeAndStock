package com.stock.stock.service;

import com.stock.stock.domain.AppUser;
import com.stock.stock.domain.RegistrationRequest;
import com.stock.stock.domain.StoreStand;
import com.stock.stock.dto.*;
import com.stock.stock.repository.AppUserRepository;
import com.stock.stock.repository.RegistrationRequestRepository;
import com.stock.stock.repository.SaleRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AdminService {

    private final RegistrationRequestRepository registrationRequestRepository;
    private final AppUserRepository appUserRepository;
    private final SaleRepository saleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    private static final String PASSWORD_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%&*";
    private static final SecureRandom RANDOM = new SecureRandom();

    public AdminService(RegistrationRequestRepository registrationRequestRepository,
            AppUserRepository appUserRepository,
            SaleRepository saleRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.registrationRequestRepository = registrationRequestRepository;
        this.appUserRepository = appUserRepository;
        this.saleRepository = saleRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    // ─── Registration Requests ─────────────────────────────────────────

    public List<RegistrationRequestResponseDto> getRegistrationRequests(String status) {
        List<RegistrationRequest> requests;
        if (status != null && !status.isBlank()) {
            requests = registrationRequestRepository.findByStatus(
                    RegistrationRequest.RequestStatus.valueOf(status.toUpperCase()));
        } else {
            requests = registrationRequestRepository.findAll();
        }
        return requests.stream().map(this::toRegistrationDto).collect(Collectors.toList());
    }

    public GeneratedCredentialsDto generateCredentials(Long requestId) {
        RegistrationRequest request = registrationRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Registration request not found"));

        String username = generateUsername(request.getEmail(),
                request.getStoreStand() != null ? request.getStoreStand().getMallName() : "user");
        String password = generatePassword(12);

        return GeneratedCredentialsDto.builder()
                .username(username)
                .password(password)
                .build();
    }

    @Transactional
    public AdminRegistrationResponseDto approveRequest(Long requestId, ApproveRequestDto dto) {
        RegistrationRequest request = registrationRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Registration request not found"));

        if (request.getStatus() != RegistrationRequest.RequestStatus.PENDING) {
            throw new RuntimeException("Request has already been " + request.getStatus().name().toLowerCase());
        }

        // Check if email already exists as a user
        if (appUserRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("A user with email " + request.getEmail() + " already exists");
        }

        // Use provided or generate credentials
        String mallName = request.getStoreStand() != null ? request.getStoreStand().getMallName() : "user";
        String username = (dto.getUsername() != null && !dto.getUsername().isBlank())
                ? dto.getUsername()
                : generateUsername(request.getEmail(), mallName);
        String plainPassword = (dto.getPassword() != null && !dto.getPassword().isBlank())
                ? dto.getPassword()
                : generatePassword(12);

        // Ensure username uniqueness
        String finalUsername = ensureUniqueUsername(username);

        // Determine role
        String role = (dto.getRole() != null && !dto.getRole().isBlank())
                ? dto.getRole()
                : "ROLE_EMPLOYEE";

        // Create the user — @ManyToOne allows multiple users per store
        AppUser newUser = AppUser.builder()
                .username(finalUsername)
                .email(request.getEmail())
                .password(passwordEncoder.encode(plainPassword))
                .roles(role)
                .enabled(true)
                .storeStand(request.getStoreStand())
                .build();
        appUserRepository.save(newUser);

        // Update the request
        request.setStatus(RegistrationRequest.RequestStatus.APPROVED);
        request.setReviewedAt(LocalDateTime.now());
        registrationRequestRepository.save(request);

        // Send email
        emailService.sendApprovalEmail(request.getEmail(), finalUsername, plainPassword);

        log.info("Approved registration request {} → user {}", requestId, finalUsername);

        return AdminRegistrationResponseDto.builder()
                .requestId(requestId)
                .email(request.getEmail())
                .username(finalUsername)
                .password(plainPassword)
                .role(role)
                .storeMallName(request.getStoreStand() != null ? request.getStoreStand().getMallName() : "N/A")
                .status("APPROVED")
                .message("User created successfully")
                .build();
    }

    @Transactional
    public RegistrationRequestResponseDto rejectRequest(Long requestId, RejectRequestDto dto) {
        RegistrationRequest request = registrationRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Registration request not found"));

        if (request.getStatus() != RegistrationRequest.RequestStatus.PENDING) {
            throw new RuntimeException("Request has already been " + request.getStatus().name().toLowerCase());
        }

        request.setStatus(RegistrationRequest.RequestStatus.REJECTED);
        request.setReviewedAt(LocalDateTime.now());
        request.setAdminNotes(dto != null ? dto.getAdminNotes() : null);
        registrationRequestRepository.save(request);

        // Send email
        emailService.sendRejectionEmail(request.getEmail(),
                dto != null ? dto.getAdminNotes() : null);

        log.info("Rejected registration request {}", requestId);

        return toRegistrationDto(request);
    }

    // ─── Active User Management ────────────────────────────────────────

    public List<AdminUserResponseDto> getAllUsers() {
        return appUserRepository.findAll().stream()
                .map(this::toUserDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public AdminUserResponseDto updateUserRole(Long userId, RoleUpdateRequest dto) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setRoles(dto.getRole());
        appUserRepository.save(user);
        log.info("Updated role of user {} to {}", user.getUsername(), dto.getRole());
        return toUserDto(user);
    }

    @Transactional
    public void deleteUser(Long userId) {
        AppUser user = appUserRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        // Delete all sales linked to this user first (FK constraint)
        var sales = saleRepository.findByAppUser(user);
        if (!sales.isEmpty()) {
            log.info("Deleting {} sales linked to user {}", sales.size(), user.getUsername());
            saleRepository.deleteAll(sales);
        }

        appUserRepository.delete(user);
        log.info("Deleted user {}", user.getUsername());
    }

    // ─── Username & Password Generation ────────────────────────────────

    String generateUsername(String email, String mallName) {
        String emailPrefix = email.split("@")[0];

        String namePart = emailPrefix
                .toLowerCase()
                .replaceAll("[.\\-+]", "_")
                .replaceAll("[^a-z_]", "")
                .replaceAll("_+", "_")
                .replaceAll("^_|_$", "");

        String storePart = mallName.toLowerCase()
                .replaceAll("[^a-z0-9 ]", "")
                .split("\\s+")[0];

        return namePart + "_" + storePart;
    }

    private String ensureUniqueUsername(String base) {
        if (appUserRepository.findByUsername(base).isEmpty()) {
            return base;
        }
        int counter = 2;
        while (appUserRepository.findByUsername(base + "_" + counter).isPresent()) {
            counter++;
        }
        return base + "_" + counter;
    }

    String generatePassword(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(PASSWORD_CHARS.charAt(RANDOM.nextInt(PASSWORD_CHARS.length())));
        }
        return sb.toString();
    }

    // ─── Mappers ───────────────────────────────────────────────────────

    private RegistrationRequestResponseDto toRegistrationDto(RegistrationRequest r) {
        return RegistrationRequestResponseDto.builder()
                .id(r.getId())
                .email(r.getEmail())
                .storeStandId(r.getStoreStand() != null ? r.getStoreStand().getId() : null)
                .storeMallName(r.getStoreStand() != null ? r.getStoreStand().getMallName() : "N/A")
                .storeCity(r.getStoreStand() != null ? r.getStoreStand().getCity().toString() : "N/A")
                .status(r.getStatus())
                .createdAt(r.getCreatedAt())
                .message(null)
                .build();
    }

    private AdminUserResponseDto toUserDto(AppUser u) {
        return AdminUserResponseDto.builder()
                .id(u.getId())
                .username(u.getUsername())
                .email(u.getEmail())
                .roles(u.getRoles())
                .enabled(u.getEnabled())
                .storeStandId(u.getStoreStand() != null ? u.getStoreStand().getId() : null)
                .storeMallName(u.getStoreStand() != null ? u.getStoreStand().getMallName() : null)
                .storeCity(u.getStoreStand() != null ? u.getStoreStand().getCity().toString() : null)
                .build();
    }
}
