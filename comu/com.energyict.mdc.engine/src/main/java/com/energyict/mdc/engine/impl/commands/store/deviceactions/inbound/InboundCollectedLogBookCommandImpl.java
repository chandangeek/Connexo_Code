package com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound;

import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.LogBooksCommandImpl;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;

import com.energyict.mdc.tasks.LogBooksTask;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 9/6/13
 * Time: 8:41 AM
 */
public class InboundCollectedLogBookCommandImpl extends LogBooksCommandImpl {

    private final List<ServerCollectedData> collectedData;

    public InboundCollectedLogBookCommandImpl(LogBooksTask logBooksTask, OfflineDevice device, CommandRoot commandRoot, ComTaskExecution comTaskExecution, List<ServerCollectedData> collectedData, DeviceDataService deviceDataService) {
        super(logBooksTask, device, commandRoot, comTaskExecution);
        this.collectedData = collectedData;
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        this.addListOfCollectedDataItems(collectedData);
    }
}
