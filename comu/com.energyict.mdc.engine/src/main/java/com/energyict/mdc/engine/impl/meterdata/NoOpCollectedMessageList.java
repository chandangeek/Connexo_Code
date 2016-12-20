package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.MeterDataStoreCommand;
import com.energyict.mdc.engine.impl.commands.store.NoopDeviceCommand;
import com.energyict.mdc.upl.meterdata.CollectedMessage;
import com.energyict.mdc.upl.meterdata.CollectedMessageList;
import com.energyict.mdc.upl.meterdata.identifiers.MessageIdentifier;
import com.energyict.mdc.upl.tasks.DataCollectionConfiguration;

import java.util.Collections;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 22/03/13
 * Time: 15:20
 */
public class NoOpCollectedMessageList extends CollectedDeviceData implements CollectedMessageList {

    @Override
    public void addCollectedMessage(CollectedMessage collectedMessage) {
    }

    @Override
    public void addCollectedMessages(CollectedMessageList collectedMessages) {
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