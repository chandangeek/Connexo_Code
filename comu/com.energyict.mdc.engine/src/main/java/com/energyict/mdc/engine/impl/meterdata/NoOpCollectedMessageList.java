package com.energyict.mdc.engine.impl.meterdata;

import com.energyict.mdc.engine.impl.commands.store.DeviceCommand;
import com.energyict.mdc.engine.impl.commands.store.NoopDeviceCommand;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.protocol.api.device.data.CollectedMessage;
import com.energyict.mdc.protocol.api.device.data.CollectedMessageList;
import com.energyict.mdc.protocol.api.device.data.DataCollectionConfiguration;

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
    public DeviceCommand toDeviceCommand(IssueService issueService) {
        return new NoopDeviceCommand();
    }

    @Override
    public boolean isConfiguredIn(DataCollectionConfiguration configuration) {
        return false;
    }
}
