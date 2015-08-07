package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.device.data.tasks.history.ComSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.exceptions.MessageSeeds;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.issues.Problem;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;

/**
 * Provides a proper response to the actual device via the InboundDeviceProtocol.
 * It for some reason this fails, we try to log this in the ComSession
 */
public class ProvideInboundResponseDeviceCommandImpl extends DeviceCommandImpl implements ProvideInboundResponseDeviceCommand {

    private final InboundCommunicationHandler inboundCommunicationHandler;
    private final InboundDeviceProtocol inboundDeviceProtocol;
    private final ExecutionContext executionContext;
    private InboundDeviceProtocol.DiscoverResponseType responseType = InboundDeviceProtocol.DiscoverResponseType.SUCCESS; // optimistic

    public ProvideInboundResponseDeviceCommandImpl(InboundCommunicationHandler inboundCommunicationHandler, InboundDeviceProtocol inboundDeviceProtocol, ExecutionContext executionContext) {
        super(executionContext.getDeviceCommandServiceProvider());
        this.inboundCommunicationHandler = inboundCommunicationHandler;
        this.inboundDeviceProtocol = inboundDeviceProtocol;
        this.executionContext = executionContext;
    }

    @Override
    protected void doExecute(ComServerDAO comServerDAO) {
        try {
            inboundCommunicationHandler.provideResponse(inboundDeviceProtocol, responseType);
        } catch (Exception e) {
            executionContext.getStoreCommand().getChildren().stream().filter(deviceCommand -> deviceCommand instanceof CreateComSessionDeviceCommand)
                    .findFirst().ifPresent(deviceCommand -> {
                CreateComSessionDeviceCommand createComSessionDeviceCommand = (CreateComSessionDeviceCommand) deviceCommand;
                createComSessionDeviceCommand.addIssue(CompletionCode.ConnectionError,
                        createCouldNotProvideProperResponseIssue(), executionContext.getComTaskExecution());
                createComSessionDeviceCommand.updateSuccessIndicator(ComSession.SuccessIndicator.Broken);
            });
        }
    }

    private Problem createCouldNotProvideProperResponseIssue() {
        return ((ServiceProvider) executionContext.getDeviceCommandServiceProvider()).issueService().newProblem(inboundDeviceProtocol, MessageSeeds.INBOUND_DATA_RESPONSE_FAILURE.getKey());
    }

    @Override
    protected void toJournalMessageDescription(DescriptionBuilder builder, ComServer.LogLevel serverLogLevel) {
        if (isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.INFO)) {
            builder.addProperty("response to inbound device").append(responseType);
        }
    }

    @Override
    public String getDescriptionTitle() {
        return "Provide response to inbound device";
    }

    @Override
    public void dataStorageFailed() {
        this.responseType = InboundDeviceProtocol.DiscoverResponseType.STORING_FAILURE;
    }
}
