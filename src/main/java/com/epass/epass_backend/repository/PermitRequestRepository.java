package com.epass.epass_backend.repository;

import com.epass.epass_backend.model.PermitRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PermitRequestRepository extends JpaRepository<PermitRequest, Long> {
    List<PermitRequest> findByUserId(Long userId);
    List<PermitRequest> findByStatus(PermitRequest.Status status);
}