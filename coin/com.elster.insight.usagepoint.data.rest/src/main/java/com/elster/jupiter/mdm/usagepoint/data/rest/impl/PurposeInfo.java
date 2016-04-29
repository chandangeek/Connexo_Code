package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.metering.config.MetrologyContract;
import java.util.List;

public class PurposeInfo {
    public long id;
    public String name;
    public boolean required;
    public boolean active;
    public String mRID;
    public IdWithNameInfo status;
    public List<MeterRoleInfo> meterRoles;

    public PurposeInfo() {
    }

    public static PurposeInfo asInfo(MetrologyContract metrologyContract) {
        PurposeInfo purposeInfo = new PurposeInfo();
        purposeInfo.id = metrologyContract.getMetrologyPurpose().getId();
        purposeInfo.name = metrologyContract.getMetrologyPurpose().getName();
        purposeInfo.required = metrologyContract.isMandatory();
        purposeInfo.active = purposeInfo.required;
        IdWithNameInfo status = new IdWithNameInfo();
        status.id = metrologyContract.getStatus().getKey().equals("COMPLETE") ? "complete" : "incomplete";
        status.name = metrologyContract.getStatus().getName();
        purposeInfo.status = status;
        return purposeInfo;
    }
}
