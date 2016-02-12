package com.elster.jupiter.bpm;


import java.util.List;
import java.util.Map;

public interface BpmProcessDefinition {

    void revokePrivileges(List<BpmProcessPrivilege> processPrivileges);

    void grantPrivileges(List<BpmProcessPrivilege> targetPrivileges);

    long getId();

    String getProcessName();

    String getAssociation();

    String getVersion();

    String getStatus();

    void update();

    void setStatus(String status);

    void delete();

    List<BpmProcessPrivilege> getPrivileges();

    List<Map<String, String>> getAssociationData();

    void setAssociationData(List<Map<String, String>> associationData);

}
