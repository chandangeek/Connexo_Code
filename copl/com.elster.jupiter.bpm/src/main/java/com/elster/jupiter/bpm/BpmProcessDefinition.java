/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.elster.jupiter.bpm;


import com.elster.jupiter.properties.HasDynamicPropertiesWithValues;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ProviderType
public interface BpmProcessDefinition extends HasDynamicPropertiesWithValues {

    void revokePrivileges(List<BpmProcessPrivilege> processPrivileges);

    @Deprecated
    void revokeProcessDeviceStates(List<BpmProcessDeviceState> processDeviceStates);

    @Deprecated
    void grantProcessDeviceStates(List<BpmProcessDeviceState> processDeviceStates);

    void grantPrivileges(List<BpmProcessPrivilege> targetPrivileges);

    long getId();

    String getProcessName();

    public String getAssociation();

    void setAssociation(String association);

    Optional<ProcessAssociationProvider> getAssociationProvider();

    String getVersion();

    long getVersionDB();

    String getStatus();

    String getAppKey();

    void setStatus(String status);

    void setAppKey(String appKey);

    void save();

    void delete();

    List<BpmProcessPrivilege> getPrivileges();

    void setPrivileges(List<BpmProcessPrivilege> targetPrivileges);

    @Deprecated
    List<BpmProcessDeviceState> getProcessDeviceStates();

    void setProperties(Map<String, Object> propertyMap);
}
