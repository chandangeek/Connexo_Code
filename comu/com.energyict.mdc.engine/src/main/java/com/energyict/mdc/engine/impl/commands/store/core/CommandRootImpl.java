package com.energyict.mdc.engine.impl.commands.store.core;

import com.energyict.mdc.device.data.tasks.ComTaskExecution;
import com.energyict.mdc.engine.impl.commands.collect.ComCommand;
import com.energyict.mdc.engine.impl.commands.collect.ComCommandType;
import com.energyict.mdc.engine.impl.commands.collect.CommandRoot;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.logging.LogLevel;
import com.energyict.mdc.issues.Issue;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.security.DeviceProtocolSecurityPropertySet;
import com.energyict.mdc.tasks.ComTask;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Implementation for a {@link CommandRoot}
 *
 * @author gna
 * @since 10/05/12 - 14:29
 */
public class CommandRootImpl implements CommandRoot {

    private final boolean exposeStoringException;
    private final ServiceProvider serviceProvider;
    private boolean connectionEstablished = true;
    private boolean connectionErrorOccurred;
    private ExecutionContext executionContext;
    private Set<GroupedDeviceCommand> groupedDeviceCommands = new LinkedHashSet<>();
    private Throwable generalSetupError;
    private List<? extends ComTaskExecution> scheduledButNotPreparedComTaskExecutions = new ArrayList<>();

    public CommandRootImpl(ExecutionContext executionContext, ServiceProvider serviceProvider) {
        this(executionContext, serviceProvider, false);
    }

    public CommandRootImpl(ExecutionContext executionContext, ServiceProvider serviceProvider, boolean exposeStoringException) {
        this.executionContext = executionContext;
        this.serviceProvider = serviceProvider;
        this.exposeStoringException = exposeStoringException;
    }

    @Override
    public ServiceProvider getServiceProvider() {
        return serviceProvider;
    }

    @Override
    public void execute(boolean connectionEstablished) {
        this.connectionEstablished = connectionEstablished;
        if (hasGeneralSetupErrorOccurred()) {
            executionContext.connectionLogger.taskExecutionFailed(generalSetupError, getCurrentThreadName(), "General setup");
            executeUnpreparedComTaskExecutions();
        } else {
            for (GroupedDeviceCommand groupedDeviceCommand : this.groupedDeviceCommands) {
                try {
                    groupedDeviceCommand.perform(executionContext);
                } catch (Throwable e) {
                    // we really should not get here, log stuff and continue with the others
                    executionContext.connectionLogger.taskExecutionFailed(e, getCurrentThreadName(), getComTasksDescription(), executionContext.getComTaskExecution().getDevice().getName());
                }
            }
        }
    }

    private String getComTasksDescription() {
        return executionContext.getComTaskExecution().getComTask().getName();
    }

    private String getCurrentThreadName() {
        return Thread.currentThread().getName();
    }

    @Override
    public GroupedDeviceCommand getOrCreateGroupedDeviceCommand(OfflineDevice offlineDevice, DeviceProtocol deviceProtocol, DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet) {
        for (GroupedDeviceCommand groupedDeviceCommand : this.groupedDeviceCommands) {
            if (groupedDeviceCommand.getOfflineDevice().getId() == offlineDevice.getId() &&
                    isSameDeviceProtocolSecurityPropertySet(deviceProtocolSecurityPropertySet, groupedDeviceCommand)) {
                return groupedDeviceCommand;
            }
        }
        GroupedDeviceCommand groupedDeviceCommand = new GroupedDeviceCommand(this, offlineDevice, deviceProtocol, deviceProtocolSecurityPropertySet);
        this.groupedDeviceCommands.add(groupedDeviceCommand);
        return groupedDeviceCommand;
    }

    @Override
    public void removeAllGroupedDeviceCommands() {
        this.groupedDeviceCommands.clear();
    }

    @Override
    public Map<ComCommandType, ComCommand> getCommands() {
        Map<ComCommandType, ComCommand> allCommands = new LinkedHashMap<>();
        for (GroupedDeviceCommand groupedDeviceCommand : this.groupedDeviceCommands) {
            allCommands.putAll(groupedDeviceCommand.getCommands());
        }
        return allCommands;
    }

    @Override
    public ExecutionContext getExecutionContext() {
        return this.executionContext;
    }

    //    @Override
    public List<Issue> getIssues() {
        return Collections.emptyList(); // All issues belong to sub commands, the root has no own issues
    }

    protected LogLevel defaultJournalingLogLevel() {
        return LogLevel.DEBUG;
    }

    @Override
    public boolean isExposeStoringException() {
        return exposeStoringException;
    }

    private boolean isSameDeviceProtocolSecurityPropertySet(DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet, GroupedDeviceCommand groupedDeviceCommand) {
        return (groupedDeviceCommand.getDeviceProtocolSecurityPropertySet() == null && deviceProtocolSecurityPropertySet == null) ||
                ((groupedDeviceCommand.getDeviceProtocolSecurityPropertySet() != null && deviceProtocolSecurityPropertySet != null) &&
                        (groupedDeviceCommand.getDeviceProtocolSecurityPropertySet().getAuthenticationDeviceAccessLevel() == deviceProtocolSecurityPropertySet.getAuthenticationDeviceAccessLevel() &&
                                groupedDeviceCommand.getDeviceProtocolSecurityPropertySet().getEncryptionDeviceAccessLevel() == deviceProtocolSecurityPropertySet.getEncryptionDeviceAccessLevel()));
    }

    @Override
    public Iterator<GroupedDeviceCommand> iterator() {
        return groupedDeviceCommands.iterator();
    }

    @Override
    public void connectionErrorOccurred() {
        this.connectionErrorOccurred = true;
    }

    @Override
    public boolean hasConnectionErrorOccurred() {
        return this.connectionErrorOccurred;
    }

    @Override
    public boolean hasConnectionSetupError() {
        return !connectionEstablished;
    }

    @Override
    public void generalSetupErrorOccurred(Throwable e, List<? extends ComTaskExecution> comTaskExecutions) {
        generalSetupError = e;
        scheduledButNotPreparedComTaskExecutions = comTaskExecutions;
    }

    @Override
    public boolean hasGeneralSetupErrorOccurred() {
        return generalSetupError != null;
    }

    private void executeUnpreparedComTaskExecutions() {
        GroupedDeviceCommand groupedDeviceCommandForGeneralSetupError = getOrCreateGroupedDeviceCommand(null, null, null);
        groupedDeviceCommandForGeneralSetupError.executeForGeneralSetupError(executionContext, scheduledButNotPreparedComTaskExecutions);
    }

    @Override
    public List<? extends ComTaskExecution> getScheduledButNotPreparedComTaskExecutions() {
        return scheduledButNotPreparedComTaskExecutions;
    }
}