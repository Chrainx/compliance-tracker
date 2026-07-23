package com.chrainx.compliance_tracker;

import com.chrainx.compliance_tracker.rules.Deadline;
import com.chrainx.compliance_tracker.rules.RuleEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/businesses")
public class BusinessController {

    private final BusinessRepository businessRepository;
    private final RuleEngine ruleEngine;

    @Autowired
    public BusinessController(BusinessRepository businessRepository, RuleEngine ruleEngine) {
        this.businessRepository = businessRepository;
        this.ruleEngine = ruleEngine;
    }

    @PostMapping
    public Business createBusiness(@RequestBody Business business) {
        return businessRepository.save(business);
    }

    @GetMapping
    public List<Business> getAllBusinesses() {
        return businessRepository.findAll();
    }

    @GetMapping("/{id}/deadlines")
    public ResponseEntity<List<Deadline>> getDeadlines(@PathVariable Long id) {
        Optional<Business> business = businessRepository.findById(id);

        if (business.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<Deadline> deadlines = ruleEngine.computeDeadlines(business.get(), LocalDate.now());
        return ResponseEntity.ok(deadlines);
    }
}
