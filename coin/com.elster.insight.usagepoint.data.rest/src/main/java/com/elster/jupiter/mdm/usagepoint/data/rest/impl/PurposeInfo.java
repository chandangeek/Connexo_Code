package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.config.MetrologyContract;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.validation.ValidationService;

import java.util.List;

public class PurposeInfo {
    public long id;
    public String name;
    public boolean required;
    public boolean active;
    public String mRID;
    public IdWithNameInfo status;
    public List<MeterRoleInfo> meterRoles;
    public Long version;
    public Long lastChecked;

    public PurposeInfo() {
    }

    public static PurposeInfo asInfo(MetrologyContract metrologyContract, UsagePoint usagePoint, ValidationService validationService) {
        PurposeInfo purposeInfo = new PurposeInfo();
        purposeInfo.id = metrologyContract.getId();
        purposeInfo.name = metrologyContract.getMetrologyPurpose().getName();
        purposeInfo.required = metrologyContract.isMandatory();
        purposeInfo.active = purposeInfo.required;
        IdWithNameInfo status = new IdWithNameInfo();
        status.id = metrologyContract.getStatus(usagePoint).getKey().equals("COMPLETE") ? "complete" : "incomplete";
        status.name = metrologyContract.getStatus(usagePoint).getName();
        purposeInfo.status = status;
        purposeInfo.version = metrologyContract.getVersion();
        usagePoint.getEffectiveMetrologyConfiguration().get().getChannelsContainer(metrologyContract)
                .ifPresent(channelsContainer -> validationService.getLastChecked(channelsContainer).ifPresent(lastChecked -> purposeInfo.lastChecked = lastChecked.toEpochMilli()));
        return purposeInfo;
    }
}
