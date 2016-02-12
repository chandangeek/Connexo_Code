package com.energyict.mdc.bpm.impl;


import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.energyict.mdc.bpm.BpmProcessDeviceState;
import com.elster.jupiter.orm.DataModel;


import javax.inject.Inject;
import java.time.Instant;

public class BpmProcessDeviceStateImpl implements BpmProcessDeviceState {

    private long processId;
    private long deviceStateId;
    private long deviceLifeCycleId;
    private String name;
    private String deviceState;
    private final DataModel dataModel;
    private BpmProcessDefinition bpmProcessDefinition;

    @SuppressWarnings("unused")
    private Instant createTime;

    @Inject
    public BpmProcessDeviceStateImpl(DataModel dataModel) {
        this.dataModel = dataModel;
    }

    static BpmProcessDeviceStateImpl from(DataModel dataModel, BpmProcessDefinition processDefinition, long deviceStateId, long deviceLifeCycleId, String name, String deviceState) {
        return dataModel.getInstance(BpmProcessDeviceStateImpl.class).init(processDefinition, deviceStateId, deviceLifeCycleId, name, deviceState);
    }

    private BpmProcessDeviceStateImpl init(BpmProcessDefinition processDefinition, long deviceStateId, long deviceLifeCycleId, String name, String deviceState) {
        this.bpmProcessDefinition = processDefinition;
        this.processId = processDefinition.getId();
        this.setDeviceStateId(deviceStateId);
        this.setDeviceLifeCycleId(deviceLifeCycleId);
        this.setName(name);
        this.setDeviceState(deviceState);
        return this;
    }

    @Override
    public long getProcessId() {
        return processId;
    }

    @Override
    public void persist() {
        dataModel.mapper(BpmProcessDeviceState.class).persist(this);
    }

    @Override
    public void delete() {
        dataModel.mapper(BpmProcessDeviceState.class).remove(this);
    }

    @Override
    public long getDeviceStateId() {
        return deviceStateId;
    }

    @Override
    public void setDeviceStateId(long deviceStateId) {
        this.deviceStateId = deviceStateId;
    }

    @Override
    public long getDeviceLifeCycleId() {
        return deviceLifeCycleId;
    }

    @Override
    public void setDeviceLifeCycleId(long deviceLifeCycleId) {
        this.deviceLifeCycleId = deviceLifeCycleId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDeviceState() {
        return deviceState;
    }

    @Override
    public void setDeviceState(String deviceState) {
        this.deviceState = deviceState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BpmProcessDeviceStateImpl that = (BpmProcessDeviceStateImpl) o;

        if (processId != that.processId) return false;
        if (deviceStateId != that.deviceStateId) return false;
        return deviceLifeCycleId == that.deviceLifeCycleId;

    }

    @Override
    public int hashCode() {
        int result = (int) (processId ^ (processId >>> 32));
        result = 31 * result + (int) (deviceStateId ^ (deviceStateId >>> 32));
        result = 31 * result + (int) (deviceLifeCycleId ^ (deviceLifeCycleId >>> 32));
        return result;
    }
}
