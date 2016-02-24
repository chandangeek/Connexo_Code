package com.elster.insight.usagepoint.data.rest.impl;

import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.insight.usagepoint.config.rest.MetrologyConfigurationInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)

public class UsagePointInfo {
    private UsagePoint usagePoint;

    public Long id;
    public String serviceCategory;
    public Long serviceLocationId;

    public String mRID;
    public String name;
    public boolean isSdp;
    public boolean isVirtual;
    public String outageRegion;
    public String readRoute;
    public String serviceDeliveryRemark;
    public String servicePriority;
    public Long installationTime;


    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "serviceCategory")
    @JsonTypeIdResolver(BaseUsagePointDetailsInfo.UsagePointDetailsTypeResolver.class)
    public BaseUsagePointDetailsInfo techInfo;

    public List<CustomPropertySetInfo> customPropertySets;

    public long version;
    public long createTime;
    public long modTime;
    public ServiceLocationInfo serviceLocation;
    public MetrologyConfigurationInfo metrologyConfiguration;

    public UsagePointInfo() {
    }

    public UsagePointInfo(UsagePoint usagePoint) {
        this.usagePoint = usagePoint;
    }


    void addServiceLocationInfo() {
        if (usagePoint.getServiceLocation().isPresent()) {
            serviceLocation = new ServiceLocationInfo(usagePoint.getServiceLocation().get());
        }
    }

    void addMetrologyConfigurationInfo(MetrologyConfigurationInfo mci) {
        metrologyConfiguration = mci;
    }
}