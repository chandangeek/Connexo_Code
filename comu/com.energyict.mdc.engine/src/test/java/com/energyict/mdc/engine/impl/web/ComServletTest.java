package com.energyict.mdc.engine.impl.web;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.InboundComPortPool;
import com.energyict.mdc.engine.config.ServletBasedInboundComPort;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.core.inbound.InboundDiscoveryContextImpl;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.protocol.api.device.data.identifiers.DeviceIdentifier;
import com.energyict.mdc.protocol.api.inbound.InboundDeviceProtocol;
import com.energyict.mdc.protocol.api.inbound.ServletBasedInboundDeviceProtocol;
import com.energyict.mdc.protocol.pluggable.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.Clock;
import java.util.Collections;
import java.util.Optional;

import org.junit.*;
import org.junit.runner.*;
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
        when(userService.findUser(any(String.class))).thenReturn(Optional.of(user));
        when(protocolPluggableService.findInboundDeviceProtocolPluggableClassByClassName(anyString())).thenReturn(Collections.<InboundDeviceProtocolPluggableClass>emptyList());
    }

    @Test
    public void testDoGetDoesNotFail () throws IOException, ServletException {
        ComServlet comServlet = new ComServlet(mock(ServletBasedInboundComPort.class), mock(ComServerDAO.class), mock(DeviceCommandExecutor.class), this.serviceProvider);
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

    @Test
    public void testDoPostCallsDoDiscovery () throws IOException, ServletException, BusinessException {
        ServletBasedInboundDeviceProtocol inboundDeviceProtocol = mock(ServletBasedInboundDeviceProtocol.class);
        when(inboundDeviceProtocol.doDiscovery()).thenReturn(InboundDeviceProtocol.DiscoverResultType.DATA);
        InboundDeviceProtocolPluggableClass discoveryProtocolPluggableClass = mock(InboundDeviceProtocolPluggableClass.class);
        when(discoveryProtocolPluggableClass.getInboundDeviceProtocol()).thenReturn(inboundDeviceProtocol);
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
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        when(comServerDAO.findOfflineDevice(any(DeviceIdentifier.class))).thenReturn(null);
        ComServlet comServlet = new ComServlet(comPort, comServerDAO, mock(DeviceCommandExecutor.class), this.serviceProvider);
        HttpServletRequest servletRequest = mock(HttpServletRequest.class);
        HttpServletResponse servletResponse = mock(HttpServletResponse.class);

        // Business method
        comServlet.doPost(servletRequest, servletResponse);

        // Asserts
        verify(inboundDeviceProtocol).initializeDiscoveryContext(any(InboundDiscoveryContextImpl.class));
        verify(inboundDeviceProtocol).init(servletRequest, servletResponse);
        verify(inboundDeviceProtocol).doDiscovery();
    }

}