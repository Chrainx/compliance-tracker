package com.chrainx.compliance_tracker;

import com.chrainx.compliance_tracker.rules.Deadline;
import com.chrainx.compliance_tracker.rules.RuleEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// @RestController: marks this class as an HTTP handler whose return values get serialized
// straight to JSON, instead of being treated as a view template name.
// @RequestMapping: base path prefix shared by every method below.
@RestController
@RequestMapping("/api/businesses")
public class BusinessController {

    private final BusinessRepository businessRepository;
    private final WorkPassRepository workPassRepository;
    private final RuleEngine ruleEngine;

    // @Autowired: Spring sees this constructor needs a BusinessRepository, WorkPassRepository,
    // and RuleEngine, and since it already knows how to create all three (repositories are
    // auto-implemented interfaces, RuleEngine is @Component), it builds them and passes them
    // in automatically - we never call `new BusinessController(...)` ourselves.
    @Autowired
    public BusinessController(BusinessRepository businessRepository, WorkPassRepository workPassRepository, RuleEngine ruleEngine) {
        this.businessRepository = businessRepository;
        this.workPassRepository = workPassRepository;
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

    // @PathVariable: pulls the {id} segment out of the URL into this parameter.
    // ResponseEntity<T> lets us choose the actual HTTP status code returned (200 vs 404),
    // instead of Spring always defaulting to 200.
    @GetMapping("/{id}/deadlines")
    public ResponseEntity<List<Deadline>> getDeadlines(@PathVariable Long id) {
        Optional<Business> business = businessRepository.findById(id);

        if (business.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        List<WorkPass> workPasses = workPassRepository.findByBusinessId(id);
        List<Deadline> deadlines = ruleEngine.computeDeadlines(business.get(), workPasses, LocalDate.now());
        return ResponseEntity.ok(deadlines);
    }
}
