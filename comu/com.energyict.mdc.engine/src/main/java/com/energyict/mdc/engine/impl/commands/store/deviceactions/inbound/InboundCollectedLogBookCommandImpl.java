package com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound;

import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.LogBooksCommandImpl;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.meterdata.DeviceIpAddress;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedLogBook;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.LogBooksTask;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 9/6/13
 * Time: 8:41 AM
 */
public class InboundCollectedLogBookCommandImpl extends LogBooksCommandImpl {

    private final List<ServerCollectedData> collectedData;

    public InboundCollectedLogBookCommandImpl(LogBooksTask logBooksTask, OfflineDevice device, CommandRoot commandRoot, ComTaskExecution comTaskExecution, List<ServerCollectedData> collectedData, DeviceService deviceService) {
        super(logBooksTask, device, commandRoot, comTaskExecution);
        this.collectedData = collectedData;
    }

    @Override
    public String getDescriptionTitle() {
        return "Collect inbound logbook data";
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        collectedData.stream().filter(dataItem -> dataItem instanceof CollectedLogBook || dataItem instanceof DeviceIpAddress).forEach(this::addCollectedDataItem);
    }

}