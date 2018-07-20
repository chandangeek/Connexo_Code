/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.device.configuration.rest.impl;

import com.elster.jupiter.pki.impl.wrappers.symmetric.HsmSymmetricKeyFactory;
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
    public String label;


    boolean keyEncryptionMethodIsHSM(){
        return HsmSymmetricKeyFactory.KEY_ENCRYPTION_METHOD.equals(this.storageMethod);
    }
}
