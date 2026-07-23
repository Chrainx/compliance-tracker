package com.chrainx.compliance_tracker.rules;

import com.chrainx.compliance_tracker.Business;
import com.chrainx.compliance_tracker.WorkPass;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

// @Component: tells Spring "create one instance of this and make it injectable elsewhere"
// (e.g. into BusinessController's constructor) - same mechanism as @RestController, just
// without any HTTP handling attached to it.
//
// Deliberately pure: no DB or HTTP dependency here, and referenceDate is passed in rather
// than read from LocalDate.now() internally, so every rule is a plain function of its inputs
// and fully deterministic to unit-test.
@Component
public class RuleEngine {

    public List<Deadline> computeDeadlines(Business business, List<WorkPass> workPasses, LocalDate referenceDate) {
        List<Deadline> deadlines = new ArrayList<>();

        // ACRA rule: due 7 months after Financial Year End (standard, non-listed company case
        // only - see README for the listed-company variant we're not modeling).
        deadlines.add(new Deadline(
                ObligationType.ACRA_ANNUAL_RETURN,
                business.getFinancialYearEnd().plusMonths(7)
        ));

        // GST F5 rule only applies if the business is actually GST-registered.
        if (business.isGstRegistered()) {
            deadlines.add(new Deadline(
                    ObligationType.GST_F5,
                    calendarQuarterEnd(referenceDate).plusMonths(1)
            ));
        }

        // Work pass rule: one deadline per pass, since a business can have multiple employees
        // each holding their own pass with its own expiry date - unlike ACRA/GST, this isn't
        // a single deadline per business.
        for (WorkPass pass : workPasses) {
            deadlines.add(new Deadline(
                    ObligationType.WORK_PASS_RENEWAL,
                    pass.getExpiryDate()
            ));
        }

        return deadlines;
    }

    // Finds the end date of the calendar quarter (Jan-Mar, Apr-Jun, Jul-Sep, Oct-Dec) that
    // referenceDate falls in. E.g. Feb 15 -> Q1 -> March 31.
    private LocalDate calendarQuarterEnd(LocalDate referenceDate) {
        int quarterEndMonth = ((referenceDate.getMonthValue() - 1) / 3) * 3 + 3;
        return YearMonth.of(referenceDate.getYear(), quarterEndMonth).atEndOfMonth();
    }
}
