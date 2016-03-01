package com.elster.jupiter.mdm.usagepoint.data.rest.impl;

import com.elster.jupiter.cps.rest.CustomPropertySetInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collections;
import java.util.List;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)

public class UsagePointInfo {

    public Long id;
    public String serviceCategory;
    public Long serviceLocationId;
    public String serviceLocation;

    public String mRID;
    public String name;
    public Boolean isSdp;
    public Boolean isVirtual;
    public String outageRegion;
    public String readRoute;
    public String serviceDeliveryRemark;
    public String servicePriority;
    public Long installationTime;


    @JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, include = JsonTypeInfo.As.EXTERNAL_PROPERTY, property = "serviceCategory")
    @JsonTypeIdResolver(BaseUsagePointDetailsInfo.UsagePointDetailsTypeResolver.class)
    public BaseUsagePointDetailsInfo techInfo;

    public List<CustomPropertySetInfo> customPropertySets = Collections.emptyList();

    public long version;
    public long createTime;
    public long modTime;
    public IdWithNameInfo metrologyConfiguration;

    public UsagePointInfo() {
    }
}