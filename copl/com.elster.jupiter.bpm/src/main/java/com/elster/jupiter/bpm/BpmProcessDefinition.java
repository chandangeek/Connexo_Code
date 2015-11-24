package com.elster.jupiter.bpm;


import java.util.List;

public interface BpmProcessDefinition {

    public void revokePrivileges(List<BpmProcessPrivilege> processPrivileges);

    void revokeProcessDeviceStates(List<BpmProcessDeviceState> processDeviceStates);

    void grantProcessDeviceStates(List<BpmProcessDeviceState> processDeviceStates);

    void grantPrivileges(List<BpmProcessPrivilege> targetPrivileges);

    public long getId();

    public String getProcessName();

    public String getAssociation();

    public String getVersion();

    public String getStatus();

    void save();

    public void setStatus(String status);

    void delete();

    List<BpmProcessPrivilege> getPrivileges();

    List<BpmProcessDeviceState> getProcessDeviceStates();
}
