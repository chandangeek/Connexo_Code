package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ExecutionContext;

/**
 * Copyrights EnergyICT
 * Date: 05.08.15
 * Time: 16:12
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
            e.printStackTrace();
            executionContext.getStoreCommand().getChildren().stream().filter(deviceCommand -> deviceCommand instanceof ProvideInboundResponseDeviceCommand)
                    .findFirst().ifPresent(deviceCommand -> ((ProvideInboundResponseDeviceCommand) deviceCommand).dataStorageFailed());
        }
    }
}
