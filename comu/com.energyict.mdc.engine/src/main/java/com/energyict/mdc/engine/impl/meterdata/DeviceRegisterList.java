package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.engine.impl.commands.store.CollectedRegisterListDeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedRegisterList;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.inbound.DeviceIdentifier;

import java.util.List;

/**
 * @author sva
 * @since 18/01/13 - 14:21
 */

public class DeviceRegisterList extends CompositeCollectedData<CollectedRegister> implements CollectedRegisterList {

    private final DeviceIdentifier deviceIdentifier;

    public DeviceRegisterList(DeviceIdentifier deviceIdentifier) {
        this.deviceIdentifier = deviceIdentifier;
    }

    @Override
    public void addCollectedRegister(CollectedRegister collectedRegister) {
        this.add(collectedRegister);
    }

    @Override
    public List<CollectedRegister> getCollectedRegisters() {
        return this.getElements();
    }

    @Override
    public DeviceIdentifier getDeviceIdentifier() {
        return deviceIdentifier;
    }


    @Override
    public DeviceCommand toDeviceCommand(IssueService issueService) {
        return new CollectedRegisterListDeviceCommand(this);
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration configuration) {
        return configuration.isConfiguredToCollectRegisterData();
    }

}