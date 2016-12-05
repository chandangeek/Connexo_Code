package com.elster.jupiter.kore.api.v1;

import com.elster.jupiter.rest.api.util.v1.hypermedia.LinkInfo;

import java.util.List;

/**
 * Created by bvn on 4/7/16.
 */
public class UsagePointCustomPropertySetInfo extends LinkInfo<Long> {

    public String name;
    public String domainClass;
    public Boolean isRequired;
    public Boolean isVersioned;
    public Boolean isEditable;
    public Boolean isActive; // time-sliced cps has active version in current moment
    public Long startTime; // time-sliced, current version start timestamp
    public Long endTime; // time-sliced, current version end timestamp
    public Long versionId; // time-sliced, current version start timestamp or 0
    public List<CustomPropertySetAttributeInfo> properties;
}
