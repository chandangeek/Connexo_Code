package com.energyict.mdc.multisense.api.impl;

import com.elster.jupiter.metering.ServiceKind;

import java.time.Instant;
import java.util.List;

public class UsagePointInfo extends LinkInfo<Long> {

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
}