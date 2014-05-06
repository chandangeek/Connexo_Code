package com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound;

import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.MessagesCommandImpl;

import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 9/6/13
 * Time: 8:44 AM
 */
public class InboundCollectedMessageListCommandImpl extends MessagesCommandImpl {

    private final List<ServerCollectedData> collectedData;


    public InboundCollectedMessageListCommandImpl(ServerMessagesTask messagesTask, OfflineDevice device, CommandRoot commandRoot, List<ServerCollectedData> collectedData) {
        super(messagesTask, device, commandRoot);
        this.collectedData = collectedData;
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, JobExecution.ExecutionContext executionContext) {
        this.addListOfCollectedDataItems(collectedData);
    }
}
