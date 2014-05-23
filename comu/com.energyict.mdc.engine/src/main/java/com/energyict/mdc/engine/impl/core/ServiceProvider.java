package com.energyict.mdc.engine.impl.core;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Clock;
import com.energyict.protocols.mdc.channels.serial.SerialComponentService;
import com.energyict.protocols.mdc.services.SocketService;

import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.EngineService;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.services.HexService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tasks.history.TaskHistoryService;

import java.util.concurrent.atomic.AtomicReference;

/**
 * Provides access to the OSGi services that are needed by
 * the core ComServer components.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-05-08 (09:32)
 */
public interface ServiceProvider {

    public final AtomicReference<ServiceProvider> instance = new AtomicReference<>();

    public EventService eventService();

    public TransactionService transactionService();

    public Clock clock();

    public IssueService issueService();

    public HexService hexService();

    public DeviceDataService deviceDataService();

    public MdcReadingTypeUtilService mdcReadingTypeUtilService();

    public EngineService engineService();

    public UserService userService();

    public ThreadPrincipalService threadPrincipalService();

    public EngineModelService engineModelService();

    public TaskHistoryService taskHistoryService();

    public DeviceConfigurationService deviceConfigurationService();

    public ProtocolPluggableService protocolPluggableService();

    public SocketService socketService();

    public SerialComponentService serialComponentService();

}