package com.chrainx.compliance_tracker.rules;

import com.chrainx.compliance_tracker.Business;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RuleEngineTest {

    private final RuleEngine ruleEngine = new RuleEngine();

    @Test
    void computesAcraDeadline_sevenMonthsAfterFinancialYearEnd() {
        Business business = new Business();
        business.setFinancialYearEnd(LocalDate.of(2026, 12, 31));
        business.setGstRegistered(false);

        List<Deadline> deadlines = ruleEngine.computeDeadlines(business, LocalDate.of(2026, 1, 1));

        Deadline acra = deadlines.stream()
                .filter(d -> d.getObligationType() == ObligationType.ACRA_ANNUAL_RETURN)
                .findFirst()
                .orElseThrow();

        assertEquals(LocalDate.of(2027, 7, 31), acra.getDueDate());
    }

    @Test
    void gstRegisteredBusiness_getsGstDeadline_oneMonthAfterCalendarQuarterEnd() {
        Business business = new Business();
        business.setFinancialYearEnd(LocalDate.of(2026, 12, 31));
        business.setGstRegistered(true);

        // Reference date falls in Q1 (Jan-Mar) -> quarter end 2026-03-31 -> deadline 2026-04-30
        List<Deadline> deadlines = ruleEngine.computeDeadlines(business, LocalDate.of(2026, 2, 15));

        Deadline gst = deadlines.stream()
                .filter(d -> d.getObligationType() == ObligationType.GST_F5)
                .findFirst()
                .orElseThrow();

        assertEquals(LocalDate.of(2026, 4, 30), gst.getDueDate());
    }

    @Test
    void nonGstRegisteredBusiness_hasNoGstDeadline() {
        Business business = new Business();
        business.setFinancialYearEnd(LocalDate.of(2026, 12, 31));
        business.setGstRegistered(false);

        List<Deadline> deadlines = ruleEngine.computeDeadlines(business, LocalDate.of(2026, 2, 15));

        assertFalse(deadlines.stream().anyMatch(d -> d.getObligationType() == ObligationType.GST_F5));
        assertTrue(deadlines.stream().anyMatch(d -> d.getObligationType() == ObligationType.ACRA_ANNUAL_RETURN));
    }
}
