package com.elster.jupiter.bpm;



public interface BpmProcessDeviceState {

    long getProcessId();

    void persist();

    void delete();

    long getDeviceStateId();

    void setDeviceStateId(long deviceStateId);

    long getDeviceLifeCycleId();

    void setDeviceLifeCycleId(long deviceLifeCycleId);

    String getName();

    void setName(String name);

    String getDeviceState();

    void setDeviceState(String deviceState);
}
