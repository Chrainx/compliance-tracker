package com.chrainx.compliance_tracker.rules;

import java.time.LocalDate;

public class Deadline {

    private final ObligationType obligationType;
    private final LocalDate dueDate;

    public Deadline(ObligationType obligationType, LocalDate dueDate) {
        this.obligationType = obligationType;
        this.dueDate = dueDate;
    }

    public ObligationType getObligationType() {
        return obligationType;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }
}
