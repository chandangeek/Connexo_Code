package com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.LoadProfileCommandImpl;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.meterdata.DeviceIpAddress;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedLoadProfile;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.LoadProfilesTask;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 9/3/13
 * Time: 4:32 PM
 */
public class InboundCollectedLoadProfileCommandImpl extends LoadProfileCommandImpl {

    private final List<ServerCollectedData> collectedData;

    public InboundCollectedLoadProfileCommandImpl(LoadProfilesTask loadProfilesTask, OfflineDevice device, CommandRoot commandRoot, ComTaskExecution comTaskExecution, List<ServerCollectedData> collectedData) {
        super(loadProfilesTask, device, commandRoot, comTaskExecution);
        this.collectedData = collectedData;
    }

    @Override
    public String getDescriptionTitle() {
        return "Collect inbound load profile data";
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        collectedData.stream().filter(dataItem ->
                dataItem instanceof CollectedLoadProfile || dataItem instanceof DeviceIpAddress)
                .forEach(this::addCollectedDataItem);

        if (this.getLoadProfilesTask().createMeterEventsFromStatusFlags()) {
            this.getCreateMeterEventsFromStatusFlagsCommand().execute(deviceProtocol, executionContext);
        }
    }

}