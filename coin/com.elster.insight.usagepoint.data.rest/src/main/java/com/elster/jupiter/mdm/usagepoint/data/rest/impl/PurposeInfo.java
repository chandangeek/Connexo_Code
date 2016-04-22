package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.rest.util.IdWithNameInfo;

public class PurposeInfo {
    public long id;
    public String name;
    public boolean required;
    public boolean active;
    public String mRID;
    public IdWithNameInfo status;

    public PurposeInfo() {
    }

    public static PurposeInfo asInfo(MetrologyContract metrologyContract) {
        PurposeInfo purposeInfo = new PurposeInfo();
        purposeInfo.id = metrologyContract.getMetrologyPurpose().getId();
        purposeInfo.name = metrologyContract.getMetrologyPurpose().getName();
        purposeInfo.required = metrologyContract.isMandatory();
        purposeInfo.active = purposeInfo.required;
        IdWithNameInfo status = new IdWithNameInfo();

        // todo (currently mocked for test purposes)
        status.id = "incomplete";
        status.name = "Incomplete";

        purposeInfo.status = status;
        return purposeInfo;
    }
}
