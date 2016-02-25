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

    String getStatus();

    void setStatus(String status);

    void save();

    void delete();

    List<BpmProcessPrivilege> getPrivileges();

    @Deprecated
    List<BpmProcessDeviceState> getProcessDeviceStates();

    void setProperties(Map<String, Object> propertyMap);
}
