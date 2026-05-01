package com.epass.epass_backend.repository;

import com.epass.epass_backend.model.Permit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PermitRepository extends JpaRepository<Permit, Long> {
    List<Permit> findByUserId(Long userId);
}