package com.elster.jupiter.kore.api.v2;

import com.elster.jupiter.metering.ServiceKind;
import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;

import java.time.Instant;
import java.util.List;

public class UsagePointInfo extends LinkInfo<Long> {

    public String name;
    public String aliasName;
    public String description;
    public String serviceLocation;
    public ServiceKind serviceKind;
    public String mrid;
    public String outageRegion;
    public String readRoute;
    public String servicePriority;
    public Instant installationTime;
    public String serviceDeliveryRemark;
    public LinkInfo<Long> metrologyConfiguration;
    public LocationInfo location;
    public CoordinatesInfo coordinates;
    public LinkInfo<Long> detail;
    public UsagePointConnectionStateInfo connectionState;
    public boolean isSdp;
    public boolean isVirtual;
    public List<UsagePointCustomPropertySetInfo> customPropertySets;

    public List<LinkInfo> meterActivations;
    public List<LinkInfo> accountabilities;
    public List<LinkInfo> usagePointConfigurations;

}