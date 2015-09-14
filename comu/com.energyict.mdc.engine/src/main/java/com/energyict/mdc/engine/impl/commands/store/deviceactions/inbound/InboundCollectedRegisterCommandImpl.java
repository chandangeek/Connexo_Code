package com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound;

import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.RegisterCommandImpl;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.meterdata.DeviceIpAddress;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedDeviceInfo;
import com.energyict.mdc.protocol.api.device.data.CollectedRegister;
import com.energyict.mdc.protocol.api.device.data.CollectedRegisterList;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.tasks.RegistersTask;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 9/4/13
 * Time: 8:47 AM
 */
public class InboundCollectedRegisterCommandImpl extends RegisterCommandImpl {

    private final List<ServerCollectedData> collectedData;

    public InboundCollectedRegisterCommandImpl(RegistersTask registersTask, OfflineDevice device, CommandRoot commandRoot, ComTaskExecution comTaskExecution, List<ServerCollectedData> collectedData, DeviceService deviceService) {
        super(registersTask, device, commandRoot, comTaskExecution);
        this.collectedData = collectedData;
    }

    @Override
    public String getDescriptionTitle() {
        return "Collect inbound register data";
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        collectedData.stream().filter(dataItem -> dataItem instanceof CollectedRegister
                        || dataItem instanceof CollectedRegisterList
                        || dataItem instanceof CollectedDeviceInfo
                        || dataItem instanceof DeviceIpAddress
        ).forEach(this::addCollectedDataItem);
    }

}