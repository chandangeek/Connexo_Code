/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.cps.rest;

import com.elster.jupiter.cps.EditPrivilege;
import com.elster.jupiter.cps.ViewPrivilege;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
import java.util.Set;

@XmlRootElement
@JsonIgnoreProperties(ignoreUnknown = true)
public class CustomPropertySetInfo<T> {

    public long id;
    public String customPropertySetId;
    public String name;
    public String domainName;
    public String domainNameUntranslated;
    public boolean isRequired;
    public boolean isVersioned;
    public boolean isEditable;
    public Boolean isActive; // time-sliced cps has active version in current moment
    public Long startTime; // time-sliced, current version start timestamp
    public Long endTime; // time-sliced, current version end timestamp
    public Long versionId; // time-sliced, current version start timestamp or 0
    public Set<ViewPrivilege> viewPrivileges;
    public Set<EditPrivilege> editPrivileges;
    public Set<ViewPrivilege> defaultViewPrivileges;
    public Set<EditPrivilege> defaultEditPrivileges;
    public List<CustomPropertySetAttributeInfo> properties;
    public T parent;
}