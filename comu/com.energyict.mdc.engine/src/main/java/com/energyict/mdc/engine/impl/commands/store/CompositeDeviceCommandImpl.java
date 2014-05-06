package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.CanProvideDescriptionTitle;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.model.ComServer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Provides code reuse opportunities for components that
 * intend to implement the {@link CompositeDeviceCommand} interface.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-08-22 (16:49)
 */
public abstract class CompositeDeviceCommandImpl implements CompositeDeviceCommand, CanProvideDescriptionTitle {

    private ComServer.LogLevel communicationLogLevel;
    private List<DeviceCommand> commands = new ArrayList<>();

    public CompositeDeviceCommandImpl () {
        this(ComServer.LogLevel.INFO);
    }

    public CompositeDeviceCommandImpl (ComServer.LogLevel communicationLogLevel) {
        super();
        this.communicationLogLevel = communicationLogLevel;
    }

    protected CompositeDeviceCommandImpl (ComServer.LogLevel communicationLogLevel, List<DeviceCommand> commands) {
        this(communicationLogLevel);
        this.commands = new ArrayList<>(commands);
    }

    @Override
    public void add (DeviceCommand command) {
        this.addDeviceCommand(command);
    }

    @Override
    public void add (CreateComSessionDeviceCommand command) {
        this.addDeviceCommand(command);
    }

    protected void addDeviceCommand (DeviceCommand command) {
        this.commands.add(command);
    }

    @Override
    public List<DeviceCommand> getChildren () {
        return new ArrayList<>(this.commands);
    }

    @Override
    public void addAll (DeviceCommand... commands) {
        this.addAll(Arrays.asList(commands));
    }

    @Override
    public void addAll (Collection<DeviceCommand> commands) {
        for (DeviceCommand command : commands) {
            this.add(command);
        }
    }

    protected void executeAll (ComServerDAO comServerDAO) {
        for (DeviceCommand command : this.getChildren()) {
            command.execute(comServerDAO);
        }
    }

    protected void executeAllDuringShutdown (ComServerDAO comServerDAO) {
        for (DeviceCommand command : this.getChildren()) {
            command.executeDuringShutdown(comServerDAO);
        }
    }

    @Override
    public ComServer.LogLevel getJournalingLogLevel () {
        return ComServer.LogLevel.TRACE;
    }

    @Override
    public String getDescriptionTitle() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String toString () {
        StringBuilder builder = new StringBuilder();
        Iterator<DeviceCommand> commandIterator = this.getChildren().iterator();
        while (commandIterator.hasNext()) {
            DeviceCommand command = commandIterator.next();
            builder.append(command.toJournalMessageDescription(communicationLogLevel));
            if (commandIterator.hasNext()) {
                builder.append("\n");
            }
        }
        return builder.toString();
    }

    @Override
    public String toJournalMessageDescription(ComServer.LogLevel serverLogLevel) {
        return null;
    }
}