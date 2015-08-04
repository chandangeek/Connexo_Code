package com.energyict.mdc.engine.impl.commands.store;

import com.elster.jupiter.transaction.VoidTransaction;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilder;
import com.energyict.mdc.common.comserver.logging.DescriptionBuilderImpl;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.exceptions.StoringFailedException;
import com.energyict.mdc.engine.impl.core.ComServerDAO;

import com.elster.jupiter.util.time.StopWatch;
import com.energyict.mdc.engine.impl.core.ExecutionContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Provides an implementation for the {@link CompositeDeviceCommand} interface
 * that will contains all the {@link DeviceCommand}s that are part of the same
 * ComSession.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-09-17 (11:52)
 */
public class ComSessionRootDeviceCommand extends CompositeDeviceCommandImpl {

    private static final String STORING_FAILURE = "Skipped storing of this collected data, because of a problem that occurred while storing data for another communication task";
    private final ExecutionContext.ServiceProvider serviceProvider;

    private List<DeviceCommand> finalCommands = new ArrayList<>();
    private CreateComSessionDeviceCommand createComSessionDeviceCommand;
    private PublishConnectionTaskEventDeviceCommand publishConnectionTaskEventDeviceCommand;

    public ComSessionRootDeviceCommand(ExecutionContext.ServiceProvider serviceProvider) {
        this(ComServer.LogLevel.INFO, serviceProvider);
    }

    public ComSessionRootDeviceCommand(ComServer.LogLevel communicationLogLevel, ExecutionContext.ServiceProvider serviceProvider) {
        super(communicationLogLevel);
        this.serviceProvider = serviceProvider;
    }

    @Override
    public void add (CreateComSessionDeviceCommand command) {
        this.createComSessionDeviceCommand = command;
        if (this.publishConnectionTaskEventDeviceCommand != null) {
            this.publishConnectionTaskEventDeviceCommand.setCreateComSessionDeviceCommand(command);
        }
    }

    public CreateComSessionDeviceCommand getCreateComSessionDeviceCommand() {
        return createComSessionDeviceCommand;
    }

    @Override
    public void add (PublishConnectionTaskEventDeviceCommand command) {
        this.publishConnectionTaskEventDeviceCommand = command;
        if (this.createComSessionDeviceCommand != null) {
            this.publishConnectionTaskEventDeviceCommand.setCreateComSessionDeviceCommand(this.createComSessionDeviceCommand);
        }
    }

    @Override
    public void add(RescheduleExecutionDeviceCommand command) {
        this.finalCommands.add(command);
    }

    @Override
    public void add(UnlockScheduledJobDeviceCommand command) {
        this.finalCommands.add(command);
    }

    @Override
    public List<DeviceCommand> getChildren () {
        List<DeviceCommand> deviceCommands = super.getChildren();
        deviceCommands.addAll(this.finalCommands);
        if (this.createComSessionDeviceCommand != null) {
            deviceCommands.add(this.createComSessionDeviceCommand);
        }
        if (this.publishConnectionTaskEventDeviceCommand != null) {
            deviceCommands.add(this.publishConnectionTaskEventDeviceCommand);
        }
        return deviceCommands;
    }

    @Override
    public void execute (final ComServerDAO comServerDAO) {
        this.startStopWatch();
        this.broadCastFailureLoggerIfAny();
        executeAll(comServerDAO);
    }

    protected void executeAll(ComServerDAO comServerDAO) {
        doExecuteAll(comServerDAO, false);
    }

    protected void executeAllDuringShutdown(ComServerDAO comServerDAO) {
        doExecuteAll(comServerDAO, true);
    }

    /**
     * Execute the nested DeviceCommands, and after that, the ComSession DeviceCommand.
     * The ComSession DeviceCommand is always executed, at the end, regardless of any storing errors that occurred earlier.
     * <p/>
     * The full transaction is rolled back only if the execution of the ComSession DeviceCommand failed.
     */
    private void doExecuteAll(final ComServerDAO comServerDAO, final boolean shutDown) {
        final StoringFailedException[] storingFailedException = new StoringFailedException[1];

        comServerDAO.executeTransaction(new VoidTransaction(){
            @Override
            protected void doPerform() {
                //First store all the collected data
                for (DeviceCommand command : getChildren()) {
                    try {
                        if (storingFailedException[0] == null) {
                            //Execute the next DeviceCommand, as long as no previous commands have failed
                            if (shutDown) {
                                command.executeDuringShutdown(comServerDAO);
                            } else {
                                command.execute(comServerDAO);
                            }
                        } else {
                            //Set the remaining comtasks to failed if one of the devicecommands failed
                            if (command instanceof ComTaskExecutionRootDeviceCommand) {
                                ((ComTaskExecutionRootDeviceCommand) command).logErrorMessage(serviceProvider.issueService().newProblem(command, STORING_FAILURE), comServerDAO);
                            }
                        }
                    } catch (StoringFailedException t) {
                        //Note that this only occurs when storing INBOUND collected data, otherwise the exception is handled silently in the DeviceCommand itself
                        storingFailedException[0] = t;
                    }
                }
            }
        });

        //Expose any exception that occurred while storing the collected data
        if (storingFailedException[0] != null) {
            throw storingFailedException[0];
        }
    }

    private void startStopWatch() {
        if (this.createComSessionDeviceCommand != null) {
            this.createComSessionDeviceCommand.setStopWatch(new StopWatch());
        }
    }

    private void broadCastFailureLoggerIfAny () {
        if (this.createComSessionDeviceCommand != null) {
            for (DeviceCommand deviceCommand : super.getChildren()) {
                deviceCommand.logExecutionWith(this.createComSessionDeviceCommand);
            }
        }
    }

    @Override
    public void executeDuringShutdown (final ComServerDAO comServerDAO) {
        this.startStopWatch();
        this.broadCastFailureLoggerIfAny();
        comServerDAO.executeTransaction(() -> {
            executeAllDuringShutdown(comServerDAO);
            return null;
        });
    }

    @Override
    public void logExecutionWith (ExecutionLogger logger) {
        // Logging is responsibility of nested commands
    }

    @Override
    public String toJournalMessageDescription(ComServer.LogLevel serverLogLevel) {
        DescriptionBuilder builder = new DescriptionBuilderImpl(this);
        if (this.createComSessionDeviceCommand != null) {
            builder.addProperty("connectionTaskID").append(this.createComSessionDeviceCommand.getConnectionTask().getId());
        }
        else {
            builder.addProperty("connectionTaskID").append("");
        }

        if (this.isJournalingLevelEnabled(serverLogLevel, ComServer.LogLevel.INFO)) {
            StringBuilder stringBuilder = builder.addProperty("commands");

            List<DeviceCommand> deviceCommands = super.getChildren();
            if (this.createComSessionDeviceCommand != null) {
                deviceCommands.add(this.createComSessionDeviceCommand);
            }

            Iterator<DeviceCommand> commandIterator = deviceCommands.iterator();
            while (commandIterator.hasNext()) {
                DeviceCommand command = commandIterator.next();
                String messageDescription = command.toJournalMessageDescription(serverLogLevel);
                stringBuilder.append(messageDescription);
                if (commandIterator.hasNext()) {
                    stringBuilder.append(", ");
                }
            }
        }
        return builder.toString();
    }

    @Override
    public String getDescriptionTitle() {
        return "ComSession device command";
    }

}