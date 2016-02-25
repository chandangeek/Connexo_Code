package com.elster.jupiter.bpm;


import com.elster.jupiter.properties.HasDynamicPropertiesWithValues;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@ProviderType
public interface BpmProcessDefinition extends HasDynamicPropertiesWithValues {

    void revokePrivileges(List<BpmProcessPrivilege> processPrivileges);

    // old
    //void revokeProcessDeviceStates(List<BpmProcessDeviceState> processDeviceStates);

    // old
    //void grantProcessDeviceStates(List<BpmProcessDeviceState> processDeviceStates);

    void grantPrivileges(List<BpmProcessPrivilege> targetPrivileges);

    long getId();

    String getProcessName();

    //public String getAssociation();
    Optional<ProcessAssociationProvider> getAssociation();

    // new
    void setAssociation(String association);

    String getVersion();

    String getStatus();

    void setStatus(String status);

    //void save();
    void update();

    void delete();

    List<BpmProcessPrivilege> getPrivileges();

    // old
    //List<BpmProcessDeviceState> getProcessDeviceStates();

    // new
    void setProperties(Map<String, Object> propertyMap);
}
