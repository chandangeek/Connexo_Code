/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.store;

import com.energyict.mdc.common.comserver.logging.CanProvideDescriptionTitle;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.impl.core.ComServerDAO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
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

    protected ComServer.LogLevel getCommunicationLogLevel() {
        return communicationLogLevel;
    }

    @Override
    public void add (DeviceCommand command) {
        this.addDeviceCommand(command);
    }

    @Override
    public void add (CreateComSessionDeviceCommand command) {
        this.addDeviceCommand(command);
    }

    @Override
    public void add(PublishConnectionTaskEventDeviceCommand command) {
        this.addDeviceCommand(command);
    }

    @Override
    public void add(RescheduleExecutionDeviceCommand command) {
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

    /**
     * Tests if the specified server log level enables details of the
     * minimum level to be shown in journal messages.
     *
     * @param serverLogLevel The server LogLevel
     * @param minimumLevel   The minimum level that is required for a message to show up in journaling
     * @return A flag that indicates if message details of the minimum level should show up in journaling
     */
    protected boolean isJournalingLevelEnabled(ComServer.LogLevel serverLogLevel, ComServer.LogLevel minimumLevel) {
        return serverLogLevel.compareTo(minimumLevel) >= 0;
    }

    @Override
    public String getDescriptionTitle() {
        return "Aggregated device command";
    }

    @Override
    public String toString () {
        return this.toJournalMessageDescription(this.getCommunicationLogLevel());
    }

}