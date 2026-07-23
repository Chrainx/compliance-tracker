package com.chrainx.compliance_tracker;

import com.chrainx.compliance_tracker.rules.Deadline;
import com.chrainx.compliance_tracker.rules.ObligationType;
import com.chrainx.compliance_tracker.rules.RuleEngine;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BusinessControllerTest {

    private final BusinessRepository businessRepository = mock(BusinessRepository.class);
    private final RuleEngine ruleEngine = new RuleEngine();
    private final BusinessController controller = new BusinessController(businessRepository, ruleEngine);

    @Test
    void getDeadlines_returnsComputedDeadlines_forExistingBusiness() {
        Business business = new Business();
        business.setId(1L);
        business.setFinancialYearEnd(LocalDate.of(2026, 12, 31));
        business.setGstRegistered(true);

        when(businessRepository.findById(1L)).thenReturn(Optional.of(business));

        ResponseEntity<List<Deadline>> response = controller.getDeadlines(1L);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().stream()
                .anyMatch(d -> d.getObligationType() == ObligationType.ACRA_ANNUAL_RETURN));
    }

    @Test
    void getDeadlines_returns404_whenBusinessNotFound() {
        when(businessRepository.findById(99L)).thenReturn(Optional.empty());

        ResponseEntity<List<Deadline>> response = controller.getDeadlines(99L);

        assertEquals(404, response.getStatusCode().value());
    }
}
