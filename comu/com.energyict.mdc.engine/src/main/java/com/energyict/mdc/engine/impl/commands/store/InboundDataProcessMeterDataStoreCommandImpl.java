package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.device.data.tasks.history.ComTaskExecutionSession;
import com.energyict.mdc.device.data.tasks.history.CompletionCode;
import com.energyict.mdc.engine.exceptions.MessageSeeds;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.issues.Problem;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Stores the processed data in the database. If it fails, we inform the ProvideInboundResponseDeviceCommand
 * so it can provide a proper response to the device.
 */
public class InboundDataProcessMeterDataStoreCommandImpl extends MeterDataStoreCommandImpl {

    private Logger logger = Logger.getLogger(InboundDataProcessMeterDataStoreCommandImpl.class.getName());

    private final ExecutionContext executionContext;

    public InboundDataProcessMeterDataStoreCommandImpl(ServiceProvider serviceProvider, ExecutionContext executionContext) {
        super(serviceProvider);
        this.executionContext = executionContext;
    }

    @Override
    protected void doExecute(ComServerDAO comServerDAO) {
        try {
            super.doExecute(comServerDAO);
        } catch (Throwable throwable) {
            this.logger.log(Level.SEVERE, throwable.getMessage(), throwable);
            executionContext.getStoreCommand().getChildren().stream().filter(deviceCommand -> deviceCommand instanceof ProvideInboundResponseDeviceCommand)
                    .findFirst().ifPresent(deviceCommand -> ((ProvideInboundResponseDeviceCommand) deviceCommand).dataStorageFailed());
            executionContext.getStoreCommand().getChildren().stream().filter(deviceCommand -> deviceCommand instanceof CreateComSessionDeviceCommand)
                    .findFirst().ifPresent(deviceCommand -> {
                CreateComSessionDeviceCommand createComSessionDeviceCommand = (CreateComSessionDeviceCommand) deviceCommand;
                createComSessionDeviceCommand.addIssue(CompletionCode.IOError,
                        createCouldNotStoreDataIssue(), executionContext.getComTaskExecution());
                createComSessionDeviceCommand.getComSessionBuilder().incrementFailedTasks(1);
                createComSessionDeviceCommand.getComSessionBuilder().incrementSuccessFulTasks(-1);
                createComSessionDeviceCommand.getComSessionBuilder().findFor(executionContext.getComTaskExecution()).
                        ifPresent(comTaskExecutionSessionBuilder -> comTaskExecutionSessionBuilder.updateSuccessIndicator(ComTaskExecutionSession.SuccessIndicator.Failure));
            });
        }
    }

    private Problem createCouldNotStoreDataIssue() {
        return ((ServiceProvider) executionContext.getDeviceCommandServiceProvider()).issueService().newProblem(this, MessageSeeds.INBOUND_DATA_STORAGE_FAILURE.getKey());
    }
}
