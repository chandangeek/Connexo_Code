/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.commands.collect;

import com.elster.jupiter.metering.MeteringService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.transaction.TransactionService;
import com.energyict.mdc.common.protocol.DeviceProtocol;
import com.energyict.mdc.common.tasks.ComTaskExecution;
import com.energyict.mdc.device.data.DeviceMessageService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.engine.impl.commands.store.core.GroupedDeviceCommand;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.pluggable.PluggableService;
import com.energyict.mdc.protocol.api.DeviceProtocol;
import com.energyict.mdc.protocol.api.device.offline.OfflineDevice;
import com.energyict.mdc.protocol.api.services.IdentificationService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.upl.security.DeviceProtocolSecurityPropertySet;

import java.time.Clock;
import java.util.List;
import java.util.Map;

/**
 * A CommandRoot contains all {@link ComCommand ComCommands} which are to be executed
 * for a single logical entity.
 * A {@link CommandRoot} can only contain each {@link ComCommandTypes ComCommandType} once, otherwise they should
 * be merged together.
 *
 * @author gna
 * @since 10/05/12 - 11:58
 */
public interface CommandRoot extends Iterable<GroupedDeviceCommand> {

    /**
     * Gets the {@link ExecutionContext} in which
     * all the {@link ComCommand}s are running.
     *
     * @return The ExecutionContext
     */
    ExecutionContext getExecutionContext();

    /**
     * Indicates if any exceptions (during storing of underlying collected data) should be exposed to the DeviceCommandExecutor
     */
    boolean isExposeStoringException();

    /**
     * Creates a GroupedDeviceCommand for the given OfflineDevice.
     *
     * @param offlineDevice                     the given OfflineDevice
     * @param deviceProtocol
     * @param deviceProtocolSecurityPropertySet
     * @return the newly created GroupedDeviceCommand
     */
    GroupedDeviceCommand getOrCreateGroupedDeviceCommand(OfflineDevice offlineDevice, DeviceProtocol deviceProtocol, DeviceProtocolSecurityPropertySet deviceProtocolSecurityPropertySet);

    void removeAllGroupedDeviceCommands();

    /**
     * Get the List of ComCommands
     *
     * @return the requested list of ComCommands
     */
    Map<ComCommandType, ComCommand> getCommands();

    /**
     * Performs all the Commands which are required and prepared for this
     *
     * @param connectionEstablished
     */
    void execute(boolean connectionEstablished);

    void connectionErrorOccurred();

    boolean hasConnectionErrorOccurred();

    boolean hasConnectionSetupError();

    void generalSetupErrorOccurred(Throwable e, List<? extends ComTaskExecution> comTaskExecutions);

    boolean hasGeneralSetupErrorOccurred();

    void connectionInterrupted();

    boolean hasConnectionBeenInterrupted();

    List<? extends ComTaskExecution> getScheduledButNotPreparedComTaskExecutions();

    ServiceProvider getServiceProvider();

    interface ServiceProvider {

        IssueService issueService();

        Clock clock();

        Thesaurus thesaurus();

        NlsService nlsService();

        DeviceService deviceService();

        MdcReadingTypeUtilService mdcReadingTypeUtilService();

        TransactionService transactionService();

        IdentificationService identificationService();

        MeteringService meteringService();

        DeviceMessageService deviceMessageService();

        ProtocolPluggableService protocolPluggableService();

    }
}