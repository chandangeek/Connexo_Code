package com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound;

import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.LoadProfileCommand;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.ReadLoadProfileDataCommandImpl;
import com.energyict.mdc.engine.impl.core.JobExecution;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedData;

/**
 * Copyrights EnergyICT
 * Date: 9/3/13
 * Time: 4:23 PM
 */
public class InboundCollectedLoadProfileReadCommandImpl extends ReadLoadProfileDataCommandImpl {

    private final CollectedData collectedData;

    public InboundCollectedLoadProfileReadCommandImpl(LoadProfileCommand loadProfileCommand, CommandRoot commandRoot, CollectedData collectedData) {
        super(loadProfileCommand, commandRoot);
        this.collectedData = collectedData;
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext) {
        this.addCollectedDataItem(collectedData);
    }
}
