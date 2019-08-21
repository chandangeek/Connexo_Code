/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.metering.UsagePoint;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleInfo;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointLifeCycleStateInfo;
import com.elster.jupiter.usagepoint.lifecycle.rest.UsagePointTransitionInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class UsagePointInfo {

    public Long id;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    public String serviceCategory;
    public Long serviceLocationId;
    public String location;
    public String geoCoordinates;
    public LocationInfo extendedLocation;
    public CoordinatesInfo extendedGeoCoordinates;


    public String mRID;
    public String name;
    public Boolean isSdp;
    public Boolean isVirtual;
    public Boolean isReadyForLinkingMC;
    public String outageRegion;
    public String readRoute;
    public String serviceDeliveryRemark;
    public String servicePriority;
    public Long installationTime;
    public IdWithNameInfo connectionState = new IdWithNameInfo();
    public String displayMetrologyConfiguration;
    public String displayServiceCategory;
    public String displayType;
    public UsagePointLifeCycleStateInfo state;
    public UsagePointLifeCycleInfo lifeCycle;
    public UsagePointTransitionInfo transitionToPerform;
    public List<CalendarOnUsagePointInfo> calendars;
    public Long lastTransitionTime;
    public boolean hasEffectiveMCs;

    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "serviceCategory")
    @JsonTypeIdResolver(BaseUsagePointDetailsInfo.UsagePointDetailsTypeResolver.class)
    public BaseUsagePointDetailsInfo techInfo;

    public List<CustomPropertySetInfo> customPropertySets = Collections.emptyList();

    public long version;
    public long createTime;
    public long modTime;
    public MetrologyConfigurationInfo metrologyConfiguration;
    public MetrologyConfigurationInfo effectiveMetrologyConfiguration;
    public List<MeterActivationInfo> meterActivations;
    public List<MeterRoleInfo> meterRoles;

    public UsagePointInfo() {
    }

    public void writeTo(UsagePoint usagePoint) {
        usagePoint.setName(this.name);
        usagePoint.setServiceLocationString(this.extendedLocation.unformattedLocationValue);
        usagePoint.setReadRoute(this.readRoute);
        usagePoint.setServicePriority(this.servicePriority);
        usagePoint.setServiceDeliveryRemark(this.serviceDeliveryRemark);

        usagePoint.update();
    }

}
