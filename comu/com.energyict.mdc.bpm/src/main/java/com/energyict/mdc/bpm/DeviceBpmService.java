package com.energyict.mdc.bpm;

import com.elster.jupiter.bpm.BpmProcessDefinition;
import com.elster.jupiter.nls.Layer;
import com.elster.jupiter.util.conditions.Condition;
import com.elster.jupiter.util.conditions.Operator;

import java.util.List;
import java.util.Optional;


public interface DeviceBpmService {
    String COMPONENTNAME = "DBP";

    BpmProcessDeviceState createBpmProcessDeviceState(BpmProcessDefinition bpmProcessDefinition, long deviceStateId, long deviceLifeCycleId, String name, String deviceName);

    Optional<List<BpmProcessDeviceState>> findBpmProcessDeviceStates(long processId);

    void revokeProcessDeviceStates(List<BpmProcessDeviceState> processDeviceStates);

    void grantProcessDeviceStates(List<BpmProcessDeviceState> processDeviceStates);

    String getModuleName();

    String getComponentName();

    Layer getLayer();

}
