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

    String getVersion();

    String getStatus();

    void update();

    void setStatus(String status);

    void delete();

    List<BpmProcessPrivilege> getPrivileges();

    List<BpmProcessProperty> getProcessDefinitionProperties();
}
