package com.chrainx.compliance_tracker;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/businesses")
public class BusinessController {

    private final BusinessRepository businessRepository;

    @Autowired
    public BusinessController(BusinessRepository businessRepository) {
        this.businessRepository = businessRepository;
    }

    @PostMapping
    public Business createBusiness(@RequestBody Business business) {
        return businessRepository.save(business);
    }

    @GetMapping
    public List<Business> getAllBusinesses() {
        return businessRepository.findAll();
    }
}
