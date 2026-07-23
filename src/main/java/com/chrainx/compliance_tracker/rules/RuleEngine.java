package com.chrainx.compliance_tracker.rules;

import com.chrainx.compliance_tracker.Business;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Component
public class RuleEngine {

    public List<Deadline> computeDeadlines(Business business, LocalDate referenceDate) {
        List<Deadline> deadlines = new ArrayList<>();

        deadlines.add(new Deadline(
                ObligationType.ACRA_ANNUAL_RETURN,
                business.getFinancialYearEnd().plusMonths(7)
        ));

        if (business.isGstRegistered()) {
            deadlines.add(new Deadline(
                    ObligationType.GST_F5,
                    calendarQuarterEnd(referenceDate).plusMonths(1)
            ));
        }

        return deadlines;
    }

    private LocalDate calendarQuarterEnd(LocalDate referenceDate) {
        int quarterEndMonth = ((referenceDate.getMonthValue() - 1) / 3) * 3 + 3;
        return YearMonth.of(referenceDate.getYear(), quarterEndMonth).atEndOfMonth();
    }
}
