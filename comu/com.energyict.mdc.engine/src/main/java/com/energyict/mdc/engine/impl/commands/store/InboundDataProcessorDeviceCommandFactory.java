package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.meterdata.ServerCollectedData;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;

import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 05.08.15
 * Time: 16:05
 */
public class InboundDataProcessorDeviceCommandFactory extends AbstractDeviceCommandFactory{

    private final ExecutionContext executionContext;
    private final InboundCommunicationHandler inboundCommunicationHandler;
    private final InboundDeviceProtocol inboundDeviceProtocol;

    public InboundDataProcessorDeviceCommandFactory(ExecutionContext executionContext, InboundCommunicationHandler inboundCommunicationHandler, InboundDeviceProtocol inboundDeviceProtocol) {
        this.executionContext = executionContext;
        this.inboundCommunicationHandler = inboundCommunicationHandler;
        this.inboundDeviceProtocol = inboundDeviceProtocol;
    }

    @Override
    MeterDataStoreCommandImpl getMeterDataStoreCommand(DeviceCommand.ServiceProvider serviceProvider) {
        return new InboundDataProcessMeterDataStoreCommandImpl(serviceProvider, executionContext);
    }

    @Override
    public List<DeviceCommand> newForAll(List<ServerCollectedData> collectedData, DeviceCommand.ServiceProvider serviceProvider) {
        List<DeviceCommand> deviceCommands = super.newForAll(collectedData, serviceProvider);
        deviceCommands.add(createInboundResponseCommand());
        return deviceCommands;
    }

    private ProvideInboundResponseDeviceCommandImpl createInboundResponseCommand() {
        return new ProvideInboundResponseDeviceCommandImpl(executionContext.getDeviceCommandServiceProvider(), inboundCommunicationHandler, inboundDeviceProtocol);
    }
}
