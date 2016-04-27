package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;

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
    public String location;

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
        location = usagePoint.getLocation().map(Location::toString).orElse(usagePoint.getGeoCoordinates()
                .map(coordinates -> coordinates.getCoordinates().toString()).orElse(""));
    }

    public void writeTo(UsagePoint usagePoint) {
        usagePoint.setName(this.name);
        usagePoint.setInstallationTime(Instant.ofEpochMilli(this.installationTime));
        usagePoint.update();
    }
}