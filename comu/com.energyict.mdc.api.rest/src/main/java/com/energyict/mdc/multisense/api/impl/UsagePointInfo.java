package com.energyict.mdc.multisense.api.impl;

import java.time.Instant;
import java.util.List;

public class UsagePointInfo extends LinkInfo {

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

    public LinkInfo serviceCategory;
    public List<LinkInfo> meterActivations;
    public List<LinkInfo> accountabilities;
    public List<LinkInfo> usagePointConfigurations;
}