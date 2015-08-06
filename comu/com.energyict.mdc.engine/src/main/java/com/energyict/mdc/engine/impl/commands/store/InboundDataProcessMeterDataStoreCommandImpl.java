package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ExecutionContext;

/**
 * Stores the processed data in the database. If it fails, we inform the ProvideInboundResponseDeviceCommand
 * so it can provide a proper response to the device.
 */
public class InboundDataProcessMeterDataStoreCommandImpl extends MeterDataStoreCommandImpl {

    private final ExecutionContext executionContext;

    public InboundDataProcessMeterDataStoreCommandImpl(ServiceProvider serviceProvider, ExecutionContext executionContext) {
        super(serviceProvider);
        this.executionContext = executionContext;
    }

    @Override
    protected void doExecute(ComServerDAO comServerDAO) {
        try {
            super.doExecute(comServerDAO);
        } catch (Exception e) {
            executionContext.getStoreCommand().getChildren().stream().filter(deviceCommand -> deviceCommand instanceof ProvideInboundResponseDeviceCommand)
                    .findFirst().ifPresent(deviceCommand -> ((ProvideInboundResponseDeviceCommand) deviceCommand).dataStorageFailed());
        }
    }
}
