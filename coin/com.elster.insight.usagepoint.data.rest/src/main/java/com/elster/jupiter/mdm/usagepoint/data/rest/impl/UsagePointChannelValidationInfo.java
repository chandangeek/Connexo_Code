package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import java.time.Instant;
import java.util.Set;

public class UsagePointChannelValidationInfo {
    public boolean validationActive;
    public boolean allDataValidated;
    public Instant lastChecked;
    public Set<ValidationRuleInfoWithNumber> suspectReason;
}
