package com.chrainx.compliance_tracker;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WorkPassRepository extends JpaRepository<WorkPass, Long> {

    // Spring Data JPA reads this method name and generates the query itself -
    // "findByBusinessId" becomes "SELECT * FROM work_pass WHERE business_id = ?".
    // No SQL or method body needed, same free-implementation trick as JpaRepository itself.
    List<WorkPass> findByBusinessId(Long businessId);
}
