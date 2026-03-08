package com.stock.stock.repository;

import com.stock.stock.domain.RegistrationRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RegistrationRequestRepository extends JpaRepository<RegistrationRequest, Long> {
    List<RegistrationRequest> findByStatus(RegistrationRequest.RequestStatus status);
    List<RegistrationRequest> findByEmail(String email);
}
