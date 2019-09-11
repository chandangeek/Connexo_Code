/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.hsm.model.keys.HsmJssKeyType;
import com.elster.jupiter.hsm.model.keys.SessionKeyCapability;
import com.elster.jupiter.properties.rest.PropertyInfo;
import com.elster.jupiter.rest.util.IdWithNameInfo;
import com.elster.jupiter.time.rest.TimeDurationInfo;
import com.energyict.mdc.device.configuration.rest.ExecutionLevelInfo;
import com.energyict.mdc.device.configuration.rest.SecurityAccessorInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SecurityAccessorTypeInfo {

    public long id;
    public long version;
    public String name;
    public String description;
    public KeyTypeInfo keyType;
    public String storageMethod;
    public long trustStoreId;
    public TimeDurationInfo duration;
    public List<ExecutionLevelInfo> editLevels = new ArrayList<>();
    public List<ExecutionLevelInfo> defaultEditLevels = new ArrayList<>();
    public List<ExecutionLevelInfo> viewLevels = new ArrayList<>();
    public List<ExecutionLevelInfo> defaultViewLevels = new ArrayList<>();
    public SecurityAccessorInfo defaultValue;
    public IdWithNameInfo purpose;
    public HsmJssKeyType hsmJssKeyType;
    public String label;
    public SessionKeyCapability importCapability;
    public SessionKeyCapability renewCapability;
    public int keySize;
    public String defaultServiceKey;
    public boolean isReversible;
    public IdWithNameInfo keyRenewalCommandSpecification;
    public List<PropertyInfo> properties;
    public boolean isWrapper;
    // following field will be use only for edit key renewal on security accessor on device but this is the mess we've got .... current model class is used for both
    // security accessor and security accessor on device type!
    public IdWithNameInfo wrapperIdAndName;


    public static SecurityAccessorTypeInfo getNotAvailable(String name) {
        SecurityAccessorTypeInfo securityAccessorTypeInfo = new SecurityAccessorTypeInfo();
        securityAccessorTypeInfo.id = -1;
        securityAccessorTypeInfo.name = name;
        return securityAccessorTypeInfo;
    }
}
