package com.elster.jupiter.bpm;


public interface BpmProcessDefinition {

    public long getId();

    public String getProcessName();

    public String getAssociation();

    public String getVersion();

    public String getStatus();

    void save();

    public void setStatus(String status);

    void delete();
}
