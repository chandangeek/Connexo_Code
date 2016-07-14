package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.mdm.common.rest.TimeDurationInfo;
import com.elster.jupiter.metering.rest.ReadingTypeInfo;

public class OutputInfo {
    public long id;
    public String name;
    public TimeDurationInfo interval;
    public ReadingTypeInfo readingType;
    public long version;
    public String flowUnit;
    public FormulaInfo formula;
    public UsagePointValidationStatusInfo validationInfo;

    public OutputInfo() {

    }
}
