package com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound;

import com.energyict.mdc.engine.impl.commands.store.deviceactions.VerifyLoadProfilesCommandImpl;
import com.energyict.comserver.core.JobExecution;
import com.energyict.mdc.commands.CommandRoot;
import com.energyict.mdc.commands.LoadProfileCommand;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedData;

/**
 * Copyrights EnergyICT
 * Date: 9/3/13
 * Time: 4:32 PM
 */
public class InboundCollectedLoadProfileVerifyCommandImpl extends VerifyLoadProfilesCommandImpl {

    private final CollectedData collectedData;

    public InboundCollectedLoadProfileVerifyCommandImpl(LoadProfileCommand loadProfileCommand, CommandRoot commandRoot, CollectedData collectedData) {
        super(loadProfileCommand, commandRoot);
        this.collectedData = collectedData;
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext) {
        this.addCollectedDataItem(collectedData);
    }
}
