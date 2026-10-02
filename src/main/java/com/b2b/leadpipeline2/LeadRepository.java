package com.b2b.leadpipeline2;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface LeadRepository extends JpaRepository<Lead, Integer> {
    List<Lead> findByRepId(Integer repId);
}
