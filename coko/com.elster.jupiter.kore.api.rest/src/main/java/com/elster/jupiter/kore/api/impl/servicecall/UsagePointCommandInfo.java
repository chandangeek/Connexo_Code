package com.elster.jupiter.kore.api.impl.servicecall;

import com.elster.jupiter.util.units.Quantity;

import java.util.List;

public class UsagePointCommandInfo {
    public UsagePointCommand command;
    public long effectiveTimestamp;
    public List<String> readingTypes;
    public Quantity loadLimit;
    public UsagePointCommandCallbackInfo httpCallBack;
}
