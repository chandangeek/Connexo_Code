package com.elster.jupiter.kore.api.impl;

import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.rest.util.hypermedia.LinkInfo;
import com.elster.jupiter.util.YesNoAnswer;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Instant;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "serviceKind", visible = false)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ElectricityDetailInfo.class, name = "Electricity"),
        @JsonSubTypes.Type(value = HeatDetailInfo.class, name = "Heat"),
        @JsonSubTypes.Type(value = WaterUsagePointInfo.class, name = "Water"),
        @JsonSubTypes.Type(value = GasDetailInfo.class, name = "Gas")})
abstract public class UsagePointInfo extends LinkInfo<Long> {

    public String name;
    public String aliasName;
    public String description;
    public String serviceLocation;
    public String mrid;
    public String outageRegion;
    public String readRoute;
    public String servicePriority;
    public Instant installationTime;
    public String serviceDeliveryRemark;
    public LinkInfo<Long> metrologyConfiguration;
    public List<LocationInfo> locations;

    public List<LinkInfo> meterActivations;
    public List<LinkInfo> accountabilities;
    public List<LinkInfo> usagePointConfigurations;

    public YesNoAnswer grounded;
    public YesNoAnswer collar;

    abstract UsagePointDetailBuilder createDetail(UsagePoint usagePoint, Instant instant);

    abstract ServiceKind getServiceKind();

}