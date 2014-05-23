package com.energyict.mdc.engine;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Clock;
import com.energyict.protocols.mdc.channels.serial.SerialComponentService;
import com.energyict.protocols.mdc.services.SocketService;

import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.DeviceDataService;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.services.HexService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.tasks.history.TaskHistoryService;

/**
 * Copyrights EnergyICT
 * Date: 19/05/2014
 * Time: 13:24
 */
public class FakeServiceProvider implements ServiceProvider {

    private EventService eventService;
    private TransactionService transactionService;
    private Clock clock;
    private IssueService issueService;
    private HexService hexService;
    private DeviceDataService deviceDataService;
    private MdcReadingTypeUtilService mdcReadingTypeUtilService;
    private EngineService engineService;
    private UserService userService;
    private ThreadPrincipalService threadPrincipalService;
    private EngineModelService engineModelService;
    private TaskHistoryService taskHistoryService;
    private DeviceConfigurationService deviceConfigurationService;
    private ProtocolPluggableService protocolPluggableService;
    private SocketService socketService;
    private SerialComponentService serialComponentService;

    @Override
    public EventService eventService() {
        return eventService;
    }

    @Override
    public TransactionService transactionService() {
        return transactionService;
    }

    @Override
    public Clock clock() {
        return clock;
    }

    @Override
    public IssueService issueService() {
        return issueService;
    }

    @Override
    public HexService hexService() {
        return hexService;
    }

    @Override
    public DeviceDataService deviceDataService() {
        return deviceDataService;
    }

    @Override
    public MdcReadingTypeUtilService mdcReadingTypeUtilService() {
        return mdcReadingTypeUtilService;
    }

    @Override
    public EngineService engineService() {
        return engineService;
    }

    @Override
    public UserService userService() {
        return userService;
    }

    @Override
    public ThreadPrincipalService threadPrincipalService() {
        return threadPrincipalService;
    }

    @Override
    public EngineModelService engineModelService() {
        return engineModelService;
    }

    @Override
    public TaskHistoryService taskHistoryService() {
        return taskHistoryService;
    }

    @Override
    public DeviceConfigurationService deviceConfigurationService() {
        return deviceConfigurationService;
    }

    @Override
    public ProtocolPluggableService protocolPluggableService() {
        return protocolPluggableService;
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
    }

    public void setTransactionService(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public void setIssueService(IssueService issueService) {
        this.issueService = issueService;
    }

    public void setHexService(HexService hexService) {
        this.hexService = hexService;
    }

    public void setDeviceDataService(DeviceDataService deviceDataService) {
        this.deviceDataService = deviceDataService;
    }

    public void setMdcReadingTypeUtilService(MdcReadingTypeUtilService mdcReadingTypeUtilService) {
        this.mdcReadingTypeUtilService = mdcReadingTypeUtilService;
    }

    public void setEngineService(EngineService engineService) {
        this.engineService = engineService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public void setThreadPrincipalService(ThreadPrincipalService threadPrincipalService) {
        this.threadPrincipalService = threadPrincipalService;
    }

    public void setEngineModelService(EngineModelService engineModelService) {
        this.engineModelService = engineModelService;
    }

    public void setTaskHistoryService(TaskHistoryService taskHistoryService) {
        this.taskHistoryService = taskHistoryService;
    }

    public void setDeviceConfigurationService(DeviceConfigurationService deviceConfigurationService) {
        this.deviceConfigurationService = deviceConfigurationService;
    }

    public void setProtocolPluggableService(ProtocolPluggableService protocolPluggableService) {
        this.protocolPluggableService = protocolPluggableService;
    }

    @Override
    public SocketService socketService() {
        return socketService;
    }

    public void setSocketService(SocketService socketService) {
        this.socketService = socketService;
    }

    @Override
    public SerialComponentService serialComponentService() {
        return serialComponentService;
    }

    public void setSerialComponentService(SerialComponentService serialComponentService) {
        this.serialComponentService = serialComponentService;
    }

}