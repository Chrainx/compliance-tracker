package com.chrainx.compliance_tracker;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Business {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private LocalDate financialYearEnd;

    private boolean gstRegistered;

    // Getters and setters — Spring/Hibernate needs these to read/write fields
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getFinancialYearEnd() { return financialYearEnd; }
    public void setFinancialYearEnd(LocalDate financialYearEnd) { this.financialYearEnd = financialYearEnd; }

    public boolean isGstRegistered() { return gstRegistered; }
    public void setGstRegistered(boolean gstRegistered) { this.gstRegistered = gstRegistered; }
}