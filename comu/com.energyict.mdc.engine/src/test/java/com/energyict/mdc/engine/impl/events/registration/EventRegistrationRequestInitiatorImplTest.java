/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.engine.impl.events.registration;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.engine.config.ComServer;
import com.energyict.mdc.engine.config.EngineConfigurationService;
import com.energyict.mdc.engine.config.ModemBasedInboundComPort;
import com.energyict.mdc.engine.config.OfflineComServer;
import com.energyict.mdc.engine.config.OnlineComServer;
import com.energyict.mdc.engine.config.OutboundComPort;
import com.energyict.mdc.engine.config.RemoteComServer;
import com.energyict.mdc.engine.config.ServletBasedInboundComPort;
import com.energyict.mdc.engine.config.TCPBasedInboundComPort;
import com.energyict.mdc.engine.config.UDPBasedInboundComPort;
import com.energyict.mdc.engine.config.impl.OfflineComServerImpl;
import com.energyict.mdc.engine.config.impl.OnlineComServerImpl;
import com.energyict.mdc.engine.config.impl.RemoteComServerImpl;

import com.google.inject.Provider;

import java.text.MessageFormat;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link EventRegistrationRequestInitiatorImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (15:32)
 */
@RunWith(MockitoJUnitRunner.class)
public class EventRegistrationRequestInitiatorImplTest {

    @Mock
    EngineConfigurationService engineConfigurationService;
    @Mock
    DataModel dataModel;
    @Mock
    Provider<OutboundComPort> outboundComPortProvider;
    @Mock
    Provider<ServletBasedInboundComPort> servletBasedInboundComPortProvider;
    @Mock
    Provider<ModemBasedInboundComPort> modemBasedInboundComPortProvider;
    @Mock
    Provider<TCPBasedInboundComPort> tcpBasedInboundComPortProvider;
    @Mock
    Provider<UDPBasedInboundComPort> udpBasedInboundComPortProvider;
    @Mock
    Thesaurus thesaurus;

    @Test(expected = IllegalArgumentException.class)
    public void testNonExistingComServer() {
        String comServerName = "Does not exist";
        when(this.engineConfigurationService.findComServer(comServerName)).thenReturn(Optional.empty());

        // Business method
        new EventRegistrationRequestInitiatorImpl(this.engineConfigurationService).getRegistrationURL(comServerName);

        // Expected IllegalArgumentException because the ComServer does not exist
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testOfflineComServerByName() {
        String comServerName = "Offline";
        OfflineComServer comServer = createOfflineComServer();
        when(this.engineConfigurationService.findComServer(comServerName)).thenReturn(Optional.<ComServer>of(comServer));

        // Business method
        new EventRegistrationRequestInitiatorImpl(this.engineConfigurationService).getRegistrationURL(comServerName);

        // Expected UnsupportedOperationException because OfflineComServer does not support event registration
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testOfflineComServer() {
        OfflineComServer comServer = createOfflineComServer();

        // Business method
        new EventRegistrationRequestInitiatorImpl(this.engineConfigurationService).getRegistrationURL(comServer);

        // Expected UnsupportedOperationException because OfflineComServer does not support event registration
    }

    private OfflineComServer createOfflineComServer() {
        return new OfflineComServerImpl(dataModel, outboundComPortProvider, servletBasedInboundComPortProvider, modemBasedInboundComPortProvider, tcpBasedInboundComPortProvider, udpBasedInboundComPortProvider, thesaurus);
    }

    @Test
    public void testOnlineComServer() {
        OnlineComServer comServer = createOnlineComServer();

        // Business method
        String url = new EventRegistrationRequestInitiatorImpl(this.engineConfigurationService).getRegistrationURL(comServer);

        // Asserts
        assertThat(url).isEqualTo(MessageFormat.format(ComServer.EVENT_REGISTRATION_URI_PATTERN, comServer.getServerName(), Integer.toString(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER)));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testOnlineComServerWithoutEventRegistrationUri() {
        OnlineComServer comServer = createOnlineComServer(0);

        // Business method
        new EventRegistrationRequestInitiatorImpl(this.engineConfigurationService).getRegistrationURL(comServer);

        // Expected UnsupportedOperationException because the ComServer explicitly did not support event registration
    }

    @Test
    public void testOnlineComServerByName() {
        String comServerName = "Online";
        OnlineComServer comServer = createOnlineComServer();
        when(this.engineConfigurationService.findComServer(comServerName)).thenReturn(Optional.<ComServer>of(comServer));

        // Business method
        String url = new EventRegistrationRequestInitiatorImpl(this.engineConfigurationService).getRegistrationURL(comServerName);

        // Asserts
        assertThat(url).isEqualTo(MessageFormat.format(ComServer.EVENT_REGISTRATION_URI_PATTERN, comServer.getServerName(), Integer.toString(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER)));
    }

    private OnlineComServer createOnlineComServer() {
        return createOnlineComServer(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
    }

    private OnlineComServer createOnlineComServer(int eventRegistrationPort) {
        final OnlineComServerImpl onlineComServer = new OnlineComServerImpl(dataModel, engineConfigurationService, outboundComPortProvider, servletBasedInboundComPortProvider, modemBasedInboundComPortProvider, tcpBasedInboundComPortProvider, udpBasedInboundComPortProvider, thesaurus);
        onlineComServer.setServerName("onlineComServerServerName");
        onlineComServer.setEventRegistrationPort(eventRegistrationPort);
        onlineComServer.setStatusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);
        onlineComServer.setQueryApiPort(ComServer.DEFAULT_QUERY_API_PORT_NUMBER);
        return onlineComServer;
    }

    @Test
    public void testRemoteComServer() {
        RemoteComServer comServer = createRemoteComServer();

        // Business method
        String url = new EventRegistrationRequestInitiatorImpl(this.engineConfigurationService).getRegistrationURL(comServer);

        // Asserts
        assertThat(url).isEqualTo(MessageFormat.format(ComServer.EVENT_REGISTRATION_URI_PATTERN, comServer.getServerName(), Integer.toString(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER)));
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoteComServerWithoutEventRegistrationUri() {
        RemoteComServer comServer = createRemoteComServerWithRegistrationPort(0);

        // Business method
        new EventRegistrationRequestInitiatorImpl(this.engineConfigurationService).getRegistrationURL(comServer);

        // Expected UnsupportedOperationException because the ComServer explicitly did not support event registration
    }

    @Test
    public void testRemoteComServerByName() {
        String comServerName = "Remote";
        RemoteComServer comServer = createRemoteComServer();
        when(this.engineConfigurationService.findComServer(comServerName)).thenReturn(Optional.<ComServer>of(comServer));

        // Business method
        String url = new EventRegistrationRequestInitiatorImpl(this.engineConfigurationService).getRegistrationURL(comServerName);

        // Asserts
        assertThat(url).isEqualTo(MessageFormat.format(ComServer.EVENT_REGISTRATION_URI_PATTERN, comServer.getServerName(), Integer.toString(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER)));
    }

    private RemoteComServer createRemoteComServer() {
        return createRemoteComServerWithRegistrationPort(ComServer.DEFAULT_EVENT_REGISTRATION_PORT_NUMBER);
    }

    private RemoteComServer createRemoteComServerWithRegistrationPort(int eventRegistrationPort) {
        final RemoteComServerImpl remoteComServer = new RemoteComServerImpl(dataModel, outboundComPortProvider, servletBasedInboundComPortProvider, modemBasedInboundComPortProvider, tcpBasedInboundComPortProvider, udpBasedInboundComPortProvider, thesaurus);
        remoteComServer.setServerName("remoteComServerServerName");
        remoteComServer.setEventRegistrationPort(eventRegistrationPort);
        remoteComServer.setStatusPort(ComServer.DEFAULT_STATUS_PORT_NUMBER);

        return remoteComServer;
    }
}