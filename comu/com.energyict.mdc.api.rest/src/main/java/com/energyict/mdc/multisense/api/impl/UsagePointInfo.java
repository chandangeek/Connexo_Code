package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.metering.UsagePointDetailBuilder;
import com.elster.jupiter.util.YesNoAnswer;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "serviceKind", visible = false)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ElectricityTechnicalInfo.class, name = "ELECTRICITY"),
        @JsonSubTypes.Type(value = HeatTechnicalInfo.class, name = "HEAT"),
        @JsonSubTypes.Type(value = WaterTechnicalInfo.class, name = "WATER"),
        @JsonSubTypes.Type(value = GasTechnicalInfo.class, name = "GAS")})
abstract public class UsagePointInfo extends LinkInfo<Long> {

    public String name;
    public String aliasName;
    public String description;
    public String location;
    public String mrid;
    public String outageRegion;
    public String readRoute;
    public String servicePriority;
    public Instant installationTime;
    public String serviceDeliveryRemark;
    public ServiceKind serviceKind;

    public List<LinkInfo> meterActivations;
    public List<LinkInfo> accountabilities;
    public List<LinkInfo> usagePointConfigurations;

    public YesNoAnswer grounded;
    public YesNoAnswer collar;

    abstract UsagePointDetailBuilder createDetail(UsagePoint usagePoint, Clock clock);

    abstract ServiceKind getServiceKind();

}