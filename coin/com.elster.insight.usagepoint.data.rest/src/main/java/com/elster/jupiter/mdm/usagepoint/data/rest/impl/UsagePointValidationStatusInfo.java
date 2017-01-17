package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import java.time.Instant;
import java.util.Set;

public class UsagePointValidationStatusInfo {
    public boolean validationActive;
    public boolean allDataValidated;
    public boolean hasSuspects;
    public Instant lastChecked;
    public Set<ValidationRuleInfoWithNumber> suspectReason;
    public Set<ValidationRuleInfoWithNumber> informativeReason;
    public Set<ValidationRuleInfoWithNumber> estimateReason;
}
