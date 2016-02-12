package com.elster.jupiter.bpm;

import aQute.bnd.annotation.ProviderType;

import java.util.List;
import java.util.Map;

@ProviderType
public interface BpmProcessDefinitionBuilder {
    BpmProcessDefinitionBuilder setId(String id);
    BpmProcessDefinitionBuilder setProcessName(String processName);
    BpmProcessDefinitionBuilder setAssociation(String association);
    BpmProcessDefinitionBuilder setVersion(String version);
    BpmProcessDefinitionBuilder setStatus(String status);
    BpmProcessDefinitionBuilder setAssociatedData(List<Map<String,String>> associatedData);
    BpmProcessDefinitionBuilder setPrivileges(List<BpmProcessPrivilege> privileges);
    BpmProcessDefinition create();
    }
