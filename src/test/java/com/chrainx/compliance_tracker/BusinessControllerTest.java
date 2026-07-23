package com.chrainx.compliance_tracker;

import com.chrainx.compliance_tracker.rules.Deadline;
import com.chrainx.compliance_tracker.rules.ObligationType;
import com.chrainx.compliance_tracker.rules.RuleEngine;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

// Plain unit test - no Spring context/DB involved, so it stays fast. The controller is
// instantiated directly with mocked repositories (Mockito) and a real RuleEngine, since
// RuleEngine is pure logic anyway and cheap to use as-is.
class BusinessControllerTest {

    private final BusinessRepository businessRepository = mock(BusinessRepository.class);
    private final WorkPassRepository workPassRepository = mock(WorkPassRepository.class);
    private final RuleEngine ruleEngine = new RuleEngine();
    private final BusinessController controller = new BusinessController(businessRepository, workPassRepository, ruleEngine);

    @Test
    void getDeadlines_returnsComputedDeadlines_forExistingBusiness() {
        Business business = new Business();
        business.setId(1L);
        business.setFinancialYearEnd(LocalDate.of(2026, 12, 31));
        business.setGstRegistered(true);

        when(businessRepository.findById(1L)).thenReturn(Optional.of(business));
        when(workPassRepository.findByBusinessId(1L)).thenReturn(Collections.emptyList());

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

    @Test
    void getDeadlines_includesWorkPassRenewals_whenBusinessHasWorkPasses() {
        Business business = new Business();
        business.setId(1L);
        business.setFinancialYearEnd(LocalDate.of(2026, 12, 31));
        business.setGstRegistered(false);

        WorkPass pass = new WorkPass();
        pass.setExpiryDate(LocalDate.of(2026, 11, 1));

        when(businessRepository.findById(1L)).thenReturn(Optional.of(business));
        when(workPassRepository.findByBusinessId(1L)).thenReturn(List.of(pass));

        ResponseEntity<List<Deadline>> response = controller.getDeadlines(1L);

        assertTrue(response.getBody().stream()
                .anyMatch(d -> d.getObligationType() == ObligationType.WORK_PASS_RENEWAL
                        && d.getDueDate().equals(LocalDate.of(2026, 11, 1))));
    }
}
