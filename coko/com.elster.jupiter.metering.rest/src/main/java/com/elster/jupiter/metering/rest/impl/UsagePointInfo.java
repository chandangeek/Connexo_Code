package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.cbo.PhaseCode;
import com.elster.jupiter.metering.AmiBillingReadyKind;
import com.elster.jupiter.metering.BypassStatus;
import com.elster.jupiter.metering.ElectricityDetail;
import com.elster.jupiter.metering.GasDetail;
import com.elster.jupiter.metering.HeatDetail;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointConnectedKind;
import com.elster.jupiter.metering.UsagePointDetail;
import com.elster.jupiter.metering.WaterDetail;
import com.elster.jupiter.metering.rest.impl.ServiceLocationInfo;
import com.elster.jupiter.util.YesNoAnswer;
import com.elster.jupiter.util.units.Quantity;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.Clock;
import java.time.Instant;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class UsagePointInfo {

    public long id;
    public ServiceKind serviceCategory;
    public String mRID;
    public String name;
    public Long installationTime;
    public long version;
    public long createTime;
    public long modTime;

    public UsagePointInfo() {
    }

    public UsagePointInfo(UsagePoint usagePoint, Clock clock) {
        id = usagePoint.getId();
        mRID = usagePoint.getMRID();
        serviceCategory = usagePoint.getServiceCategory().getKind();
        name = usagePoint.getName();
        installationTime = usagePoint.getInstallationTime().toEpochMilli();
        version = usagePoint.getVersion();
        createTime = usagePoint.getCreateDate().toEpochMilli();
        modTime = usagePoint.getModificationDate().toEpochMilli();
    }

    public void writeTo(UsagePoint usagePoint) {
        usagePoint.setName(this.name);
        usagePoint.setInstallationTime(Instant.ofEpochMilli(this.installationTime));
        usagePoint.update();
    }
}