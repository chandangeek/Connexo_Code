package com.elster.jupiter.metering.rest.impl;

import com.elster.jupiter.metering.Location;
import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.util.geo.SpatialCoordinates;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

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
    public String geoCoordinates;
    public List<MeterActivationInfo> meterActivations;
    public EffectiveMetrologyConfigurationOnUsagePointInfo metrologyConfigurationVersion;
    public EditLocationInfo extendedLocation;
    public CoordinatesInfo extendedGeoCoordinates;

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

        location = usagePoint.getLocation().map(Location::toString).orElse(usagePoint.getSpatialCoordinates()
                .map(SpatialCoordinates::toString).orElse(""));
        meterActivations = usagePoint.getCurrentMeterActivations().stream()
                .map(MeterActivationInfo::new)
                .collect(Collectors.toList());
        metrologyConfigurationVersion = usagePoint.getCurrentEffectiveMetrologyConfiguration()
                .map(v-> new EffectiveMetrologyConfigurationOnUsagePointInfo(v, clock))
                .orElse(null);
    }

    public void writeTo(UsagePoint usagePoint) {
        usagePoint.setName(this.name);
        usagePoint.setInstallationTime(Instant.ofEpochMilli(this.installationTime));
        usagePoint.update();
    }
}
