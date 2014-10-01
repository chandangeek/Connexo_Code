package com.energyict.mdc.engine;

import com.energyict.mdc.device.config.DeviceConfigurationService;
import com.energyict.mdc.device.data.CommunicationTaskService;
import com.energyict.mdc.device.data.ConnectionTaskService;
import com.energyict.mdc.device.data.DeviceService;
import com.energyict.mdc.device.data.LogBookService;
import com.energyict.mdc.engine.impl.core.ComChannelBasedComPortListenerImpl;
import com.energyict.mdc.engine.impl.core.ExecutionContext;
import com.energyict.mdc.engine.impl.core.RunningComServerImpl;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.events.AbstractComServerEventImpl;
import com.energyict.mdc.engine.impl.monitor.ManagementBeanFactory;
import com.energyict.mdc.engine.impl.web.EmbeddedJettyServer;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServerFactory;
import com.energyict.mdc.engine.impl.web.events.WebSocketEventPublisherFactory;
import com.energyict.mdc.engine.impl.web.events.commands.RequestParser;
import com.energyict.mdc.engine.impl.web.queryapi.WebSocketQueryApiServiceFactory;
import com.energyict.mdc.engine.model.EngineModelService;
import com.energyict.mdc.issues.IssueService;
import com.energyict.mdc.metering.MdcReadingTypeUtilService;
import com.energyict.mdc.protocol.api.services.HexService;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import com.elster.jupiter.events.EventService;
import com.elster.jupiter.nls.NlsService;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.transaction.TransactionService;
import com.elster.jupiter.users.UserService;
import com.elster.jupiter.util.time.Clock;
import com.energyict.protocols.mdc.channels.serial.SerialComponentService;
import com.energyict.protocols.mdc.services.SocketService;

/**
 * Copyrights EnergyICT
 * Date: 19/05/2014
 * Time: 13:24
 */
public class FakeServiceProvider
    implements
        ServiceProvider,
        ComChannelBasedComPortListenerImpl.ServiceProvider,
        AbstractComServerEventImpl.ServiceProvider,
        ExecutionContext.ServiceProvider,
        RequestParser.ServiceProvider,
        EmbeddedJettyServer.ServiceProvider,
        RunningComServerImpl.ServiceProvider {

    private EventService eventService;
    private TransactionService transactionService;
    private Clock clock;
    private NlsService nlsService;
    private IssueService issueService;
    private HexService hexService;
    private ConnectionTaskService connectionTaskService;
    private CommunicationTaskService communicationTaskService;
    private LogBookService logBookService;
    private DeviceService deviceService;
    private MdcReadingTypeUtilService mdcReadingTypeUtilService;
    private EngineService engineService;
    private UserService userService;
    private ThreadPrincipalService threadPrincipalService;
    private EngineModelService engineModelService;
    private DeviceConfigurationService deviceConfigurationService;
    private ProtocolPluggableService protocolPluggableService;
    private SocketService socketService;
    private SerialComponentService serialComponentService;
    private ManagementBeanFactory managementBeanFactory;
    private WebSocketQueryApiServiceFactory webSocketQueryApiServiceFactory;
    private WebSocketEventPublisherFactory webSocketEventPublisherFactory;
    private EmbeddedWebServerFactory embeddedWebServerFactory;

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

    public NlsService nlsService() {
        return nlsService;
    }

    public void setNlsService(NlsService nlsService) {
        this.nlsService = nlsService;
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
    public ConnectionTaskService connectionTaskService() {
        return connectionTaskService;
    }

    @Override
    public CommunicationTaskService communicationTaskService() {
        return this.communicationTaskService();
    }

    @Override
    public LogBookService logBookService() {
        return logBookService;
    }

    @Override
    public DeviceService deviceDataService() {
        return deviceService;
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

    public void setCommunicationTaskService(CommunicationTaskService communicationTaskService) {
        this.communicationTaskService = communicationTaskService;
    }

    public void setConnectionTaskService(ConnectionTaskService connectionTaskService) {
        this.connectionTaskService = connectionTaskService;
    }

    public void setLogBookService(LogBookService logBookService) {
        this.logBookService = logBookService;
    }

    public void setDeviceService(DeviceService deviceService) {
        this.deviceService = deviceService;
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

    @Override
    public ManagementBeanFactory managementBeanFactory() {
        return this.managementBeanFactory;
    }

    public void setManagementBeanFactory(ManagementBeanFactory managementBeanFactory) {
        this.managementBeanFactory = managementBeanFactory;
    }

    @Override
    public WebSocketQueryApiServiceFactory webSocketQueryApiServiceFactory() {
        return this.webSocketQueryApiServiceFactory;
    }

    public void setWebSocketQueryApiServiceFactory(WebSocketQueryApiServiceFactory webSocketQueryApiServiceFactory) {
        this.webSocketQueryApiServiceFactory = webSocketQueryApiServiceFactory;
    }

    @Override
    public WebSocketEventPublisherFactory webSocketEventPublisherFactory() {
        return this.webSocketEventPublisherFactory;
    }

    public void setWebSocketEventPublisherFactory(WebSocketEventPublisherFactory webSocketEventPublisherFactory) {
        this.webSocketEventPublisherFactory = webSocketEventPublisherFactory;
    }

    @Override
    public EmbeddedWebServerFactory embeddedWebServerFactory() {
        return this.embeddedWebServerFactory;
    }

    public void setEmbeddedWebServerFactory(EmbeddedWebServerFactory embeddedWebServerFactory) {
        this.embeddedWebServerFactory = embeddedWebServerFactory;
    }

}