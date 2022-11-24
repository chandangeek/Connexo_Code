/*
 * Copyright (c) 2022 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.coap;

import com.elster.jupiter.security.thread.ThreadPrincipalService;
import com.elster.jupiter.users.User;
import com.elster.jupiter.users.UserService;
import com.energyict.mdc.common.comserver.CoapBasedInboundComPort;
import com.energyict.mdc.common.comserver.ComServer;
import com.energyict.mdc.common.comserver.InboundComPort;
import com.energyict.mdc.common.comserver.InboundComPortPool;
import com.energyict.mdc.common.protocol.InboundDeviceProtocolPluggableClass;
import com.energyict.mdc.engine.impl.commands.store.DeviceCommandExecutor;
import com.energyict.mdc.engine.impl.core.ComServerDAO;
import com.energyict.mdc.engine.impl.core.inbound.InboundCommunicationHandler;
import com.energyict.mdc.engine.impl.core.inbound.InboundDiscoveryContextImpl;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.impl.monitor.InboundComPortMonitorImpl;
import com.energyict.mdc.engine.impl.monitor.InboundComPortOperationalStatisticsImpl;
import com.energyict.mdc.engine.impl.monitor.ManagementBeanFactory;
import com.energyict.mdc.protocol.api.inbound.CoapBasedInboundDeviceProtocol;
import com.energyict.mdc.protocol.pluggable.ProtocolPluggableService;
import com.energyict.mdc.upl.io.CoapBasedExchange;
import com.energyict.mdc.upl.meterdata.identifiers.DeviceIdentifier;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.time.Clock;
import java.util.Collections;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link CoapResource} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-10-19 (15:25)
 */
@RunWith(MockitoJUnitRunner.class)
public class BaseCoapResourceTest {

    private static final long COMSERVER_ID = 1;
    private static final long COMPORT_POOL_ID = COMSERVER_ID + 1;
    private static final long COMPORT_ID = COMPORT_POOL_ID + 1;
    private static final String CONTEXT_PATH = "Test";

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

    private ComServerDAO getMockedComServerDAO() {
        ComServerDAO comServerDAO = mock(ComServerDAO.class);
        when(comServerDAO.getComServerUser()).thenReturn(user);
        return comServerDAO;
    }

    @Test
    public void testDoPostCallsDoDiscovery() {
        DeviceIdentifier deviceIdentifier = mock(DeviceIdentifier.class);
        CoapBasedInboundDeviceProtocol inboundDeviceProtocol = mock(CoapBasedInboundDeviceProtocol.class);
        when(inboundDeviceProtocol.doDiscovery()).thenReturn(com.energyict.mdc.upl.InboundDeviceProtocol.DiscoverResultType.DATA);
        when(inboundDeviceProtocol.getDeviceIdentifier()).thenReturn(deviceIdentifier);
        InboundDeviceProtocolPluggableClass discoveryProtocolPluggableClass = mock(InboundDeviceProtocolPluggableClass.class);
        when(discoveryProtocolPluggableClass.getInboundDeviceProtocol()).thenReturn(inboundDeviceProtocol);
        InboundComPortPool comPortPool = mock(InboundComPortPool.class);
        when(comPortPool.getDiscoveryProtocolPluggableClass()).thenReturn(discoveryProtocolPluggableClass);
        ComServer comServer = mock(ComServer.class);
        when(comServer.getId()).thenReturn(COMSERVER_ID);
        when(comServer.getServerLogLevel()).thenReturn(ComServer.LogLevel.INFO);
        when(comPortPool.getId()).thenReturn(COMPORT_POOL_ID);
        CoapBasedInboundComPort comPort = mock(CoapBasedInboundComPort.class);
        when(comPort.getId()).thenReturn(COMPORT_ID);
        when(comPort.getComPortPool()).thenReturn(comPortPool);
        when(comPort.getComServer()).thenReturn(comServer);
        when(comPort.getContextPath()).thenReturn(CONTEXT_PATH);
        ComServerDAO comServerDAO = getMockedComServerDAO();
        when(comServerDAO.findOfflineDevice(any(DeviceIdentifier.class))).thenReturn(null);
        BaseCoapResource coapResource = new BaseCoapResource(comPort, comServerDAO, mock(DeviceCommandExecutor.class), this.serviceProvider);
        CoapExchange coapExchange = mock(CoapExchange.class);
        // Business method
        coapResource.handlePOST(coapExchange);

        // Asserts
        verify(inboundDeviceProtocol).initializeDiscoveryContext(any(InboundDiscoveryContextImpl.class));
        verify(inboundDeviceProtocol).init(any(CoapBasedExchange.class));
        verify(inboundDeviceProtocol).doDiscovery();
    }

}