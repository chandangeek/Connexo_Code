package com.energyict.mdc.engine.impl.web;

import com.elster.jupiter.properties.PropertySpec;
import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.InboundComPort;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.ServletBasedInboundComPort;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.core.inbound.InboundDiscoveryContextImpl;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.impl.monitor.InboundComPortMonitorImpl;
import com.energyict.mdc.engine.impl.monitor.InboundComPortOperationalStatisticsImpl;
import com.energyict.mdc.engine.impl.monitor.ManagementBeanFactory;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.protocol.pluggable.adapters.upl.UPLToConnexoPropertySpecAdapter;
import com.energyict.mdc.upl.InboundDiscoveryContext;
import com.energyict.mdc.upl.ServletBasedInboundDeviceProtocol;
import com.energyict.mdc.upl.meterdata.CollectedData;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.mdc.upl.properties.TypedProperties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Clock;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ComServlet} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-19 (15:25)
 */
@RunWith(MockitoJUnitRunner.class)
public class ComServletTest {

    private static final long COMSERVER_ID = 1;
    private static final long COMPORT_POOL_ID = COMSERVER_ID + 1;
    private static final long COMPORT_ID = COMPORT_POOL_ID + 1;

    @Mock
    private InboundComPortOperationalStatisticsImpl inboundComPortOperationalStatistics;
    @Mock
    private InboundComPortMonitorImpl inboundComPortMonitor;
    @Mock
    private ManagementBeanFactory managementBeanFactory;
    @Mock
    private InboundCommunicationHandler.ServiceProvider serviceProvider;
    @Mock
    private ProtocolPluggableService protocolPluggableService;
    @Mock
    private EventPublisherImpl eventPublisher;
    @Mock
    private ThreadPrincipalService threadPrincipalService;
    @Mock
    private UserService userService;
    @Mock
    private User user;

    private Clock clock = Clock.systemDefaultZone();

    @Before
    public void initializeMocks() {
        when(serviceProvider.clock()).thenReturn(this.clock);
        when(serviceProvider.protocolPluggableService()).thenReturn(this.protocolPluggableService);
        when(serviceProvider.eventPublisher()).thenReturn(this.eventPublisher);
        when(serviceProvider.threadPrincipalService()).thenReturn(this.threadPrincipalService);
        when(serviceProvider.userService()).thenReturn(this.userService);
        when(serviceProvider.managementBeanFactory()).thenReturn(managementBeanFactory);
        when(userService.findUser(any(String.class))).thenReturn(Optional.of(user));
        when(user.getLocale()).thenReturn(Optional.empty());
        when(protocolPluggableService.findInboundDeviceProtocolPluggableClassByClassName(anyString())).thenReturn(Collections.<InboundDeviceProtocolPluggableClass>emptyList());
        when(managementBeanFactory.findFor(any(InboundComPort.class))).thenReturn(Optional.of(inboundComPortMonitor));
        when(inboundComPortMonitor.getOperationalStatistics()).thenReturn(inboundComPortOperationalStatistics);
    }

    @Test
    public void testDoGetDoesNotFail() throws IOException, ServletException {
        ComServlet comServlet = new ComServlet(mock(ServletBasedInboundComPort.class), getMockedComServerDAO(), mock(DeviceCommandExecutor.class), this.serviceProvider);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);
        PrintWriter printWriter = mock(PrintWriter.class);
        when(servletResponse.getWriter()).thenReturn(printWriter);

        // Business method
        comServlet.doGet(mock(HttpServletRequest.class), servletResponse);

        // Asserts
        ArgumentCaptor<String> contentTypeCaptor = ArgumentCaptor.forClass(String.class);
        verify(servletResponse).setContentType(contentTypeCaptor.capture());
        verify(servletResponse).getWriter();
        assertThat(contentTypeCaptor.getValue()).isEqualTo("text/html");
    }

    private ComServerDAO getMockedComServerDAO() {
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        when(comServerDAO.getComServerUser()).thenReturn(user);
        return comServerDAO;
    }

    @Test
    public void testDoPostCallsDoDiscovery() throws IOException, ServletException {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        ServletBasedInboundDeviceProtocol uplInboundDeviceProtocol = mock(ServletBasedInboundDeviceProtocol.class);
        when(uplInboundDeviceProtocol.doDiscovery()).thenReturn(com.energyict.mdc.upl.InboundDeviceProtocol.DiscoverResultType.DATA);
        when(uplInboundDeviceProtocol.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        InboundDeviceProtocolPluggableClass discoveryProtocolPluggableClass = mock(InboundDeviceProtocolPluggableClass.class);
        when(discoveryProtocolPluggableClass.getInboundDeviceProtocol()).thenReturn(new UplServletBasedInboundDeviceProtocolAdapter(uplInboundDeviceProtocol));
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(comPortPool.getDiscoveryProtocolPluggableClass()).thenReturn(discoveryProtocolPluggableClass);
        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comPortPool.getId()).thenReturn(COMPORT_POOL_ID);
        ServletBasedInboundComPort comPort = mock(ServletBasedInboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        when(comPort.getComPortPool()).thenReturn(comPortPool);
        when(comPort.getComServer()).thenReturn(comServer);
        ComServerDAO comServerDAO = getMockedComServerDAO();
        when(comServerDAO.findOfflineDevice(any(DeviceIdentifier.class))).thenReturn(null);
        ComServlet comServlet = new ComServlet(comPort, comServerDAO, mock(DeviceCommandExecutor.class), this.serviceProvider);
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);

        // Business method
        comServlet.doPost(servletRequest, servletResponse);

        // Asserts
        verify(uplInboundDeviceProtocol).initializeDiscoveryContext(any(InboundDiscoveryContextImpl.class));
        verify(uplInboundDeviceProtocol).init(servletRequest, servletResponse);
        verify(uplInboundDeviceProtocol).doDiscovery();
    }

    private class UplServletBasedInboundDeviceProtocolAdapter implements InboundDeviceProtocol {
        private final ServletBasedInboundDeviceProtocol actual;

        private UplServletBasedInboundDeviceProtocolAdapter(ServletBasedInboundDeviceProtocol actual) {
            this.actual = actual;
        }

        @Override
        public void copyProperties(com.energyict.mdc.common.TypedProperties properties) {
            // Not necessary in this unit test
        }

        @Override
        public List<PropertySpec> getPropertySpecs() {
            return this.actual
                    .getUPLPropertySpecs()
                    .stream()
                    .map(UPLToConnexoPropertySpecAdapter::new)
                    .collect(Collectors.toList());
        }

        @Override
        public void initializeDiscoveryContext(InboundDiscoveryContext context) {
            actual.initializeDiscoveryContext(context);
        }

        @Override
        public InboundDiscoveryContext getContext() {
            return actual.getContext();
        }

        @Override
        public DiscoverResultType doDiscovery() {
            return actual.doDiscovery();
        }

        @Override
        public void provideResponse(DiscoverResponseType responseType) {
            actual.provideResponse(responseType);
        }

        @Override
        public DeviceIdentifier getDeviceIdentifier() {
            return actual.getDeviceIdentifier();
        }

        @Override
        public String getAdditionalInformation() {
            return actual.getAdditionalInformation();
        }

        @Override
        public List<CollectedData> getCollectedData() {
            return actual.getCollectedData();
        }

        @Override
        public boolean hasSupportForRequestsOnInbound() {
            return actual.hasSupportForRequestsOnInbound();
        }

        @Override
        public String getVersion() {
            return actual.getVersion();
        }

        @Override
        public List<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpecs() {
            return actual.getUPLPropertySpecs();
        }

        @Override
        public Optional<com.energyict.mdc.upl.properties.PropertySpec> getUPLPropertySpec(String name) {
            return actual.getUPLPropertySpec(name);
        }

        @Override
        public void setUPLProperties(TypedProperties properties) throws PropertyValidationException {
            actual.setUPLProperties(properties);
        }
    }
}