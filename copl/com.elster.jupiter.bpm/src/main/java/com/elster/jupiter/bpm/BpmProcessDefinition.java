package com.elster.jupiter.bpm;


public interface BpmProcessDefinition {

    public long getId();

    public String getProcessName();

    public String getAssociation();

    public String getVersion();

    public boolean getState();

    void save();

    public void setState(boolean state);

    void delete();
}
