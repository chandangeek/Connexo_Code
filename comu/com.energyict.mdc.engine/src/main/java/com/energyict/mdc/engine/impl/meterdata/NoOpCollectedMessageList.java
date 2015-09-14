package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.engine.impl.commands.store.NoopDeviceCommand;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;
import com.energyict.mdc.protocol.api.device.data.identifiers.MessageIdentifier;

import java.util.Collections;
import java.util.List;

/**
* Copyrights EnergyICT
* Date: 22/03/13
* Time: 15:20
*/
public class NoOpCollectedMessageList extends CollectedDeviceData implements CollectedMessageList {

    @Override
    public void addCollectedMessages(CollectedMessage collectedMessage) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CollectedMessage> getCollectedMessages() {
        return Collections.emptyList();
    }

    @Override
    public List<CollectedMessage> getCollectedMessages(MessageIdentifier messageIdentifier) {
        return Collections.emptyList();
    }

    @Override
    public DeviceCommand toDeviceCommand(MeterDataStoreCommand meterDataStoreCommand, DeviceCommand.ServiceProvider serviceProvider) {
        return new NoopDeviceCommand();
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration configuration) {
        return false;
    }

}