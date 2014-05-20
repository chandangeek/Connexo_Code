package com.energyict.mdc.engine.impl.commands.store.deviceactions.inbound;

import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.commands.collect.LogBooksCommand;
import com.energyict.mdc.engine.impl.commands.store.deviceactions.ReadLogBooksCommandImpl;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.data.CollectedData;

/**
 * Copyrights EnergyICT
 * Date: 9/6/13
 * Time: 9:12 AM
 */
public class InboundCollectedLogBookReadCommandImpl extends ReadLogBooksCommandImpl {

    private final CollectedData collectedData;

    public InboundCollectedLogBookReadCommandImpl(LogBooksCommand logBooksCommand, CommandRoot commandRoot, CollectedData collectedData) {
        super(logBooksCommand, commandRoot);
        this.collectedData = collectedData;
    }

    @Override
    public void doExecute(DeviceProtocol deviceProtocol, ExecutionContext executionContext) {
        this.addCollectedDataItem(collectedData);
    }
}
