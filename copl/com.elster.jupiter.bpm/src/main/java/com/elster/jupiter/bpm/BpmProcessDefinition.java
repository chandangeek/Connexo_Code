package com.elster.jupiter.bpm;


import com.elster.jupiter.properties.HasDynamicPropertiesWithValues;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BpmProcessDefinition extends HasDynamicPropertiesWithValues {

    void revokePrivileges(List<BpmProcessPrivilege> processPrivileges);

    void grantPrivileges(List<BpmProcessPrivilege> targetPrivileges);

    long getId();

    String getProcessName();

    Optional<ProcessAssociationProvider> getAssociation();

    void setAssociation(String association);

    String getVersion();

    String getStatus();

    void setStatus(String status);

    void update();

    void delete();

    List<BpmProcessPrivilege> getPrivileges();

    void setProperties(Map<String, Object> propertyMap);
}
