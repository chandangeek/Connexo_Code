package com.energyict.mdc.engine.impl.events.registration;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.engine.config.*;
import com.energyict.mdc.engine.config.impl.OfflineComServerImpl;
import com.energyict.mdc.engine.config.impl.OnlineComServerImpl;
import com.energyict.mdc.engine.config.impl.RemoteComServerImpl;

import java.util.Optional;

import com.google.inject.Provider;
import org.junit.*;
import org.junit.runner.*;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link EventRegistrationRequestInitiatorImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (15:32)
 */
@RunWith(MockitoJUnitRunner.class)
public class EventRegistrationRequestInitiatorImplTest {

    private static final String EVENT_REGISTRATION_URI = "ws://comserver.energyict.com/events/registration";

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

    @Test(expected = BusinessException.class)
    public void testNonExistingComServer() throws BusinessException {
        String comServerName = "Does not exist";
        when(this.engineConfigurationService.findComServer(comServerName)).thenReturn(Optional.empty());

        // Business method
        new EventRegistrationRequestInitiatorImpl(this.engineConfigurationService).getRegistrationURL(comServerName);

        // Expected BusinessException because the ComServer does not exist
    }

    @Test(expected = BusinessException.class)
    public void testOfflineComServerByName() throws BusinessException {
        String comServerName = "Offline";
        OfflineComServer comServer = createOfflineComServer();
        when(this.engineConfigurationService.findComServer(comServerName)).thenReturn(Optional.<ComServer>of(comServer));

        // Business method
        new EventRegistrationRequestInitiatorImpl(this.engineConfigurationService).getRegistrationURL(comServerName);

        // Expected BusinessException because OfflineComServer does not support event registration
    }

    @Test(expected = BusinessException.class)
    public void testOfflineComServer() throws BusinessException {
        OfflineComServer comServer = createOfflineComServer();

        // Business method
        new EventRegistrationRequestInitiatorImpl(this.engineConfigurationService).getRegistrationURL(comServer);

        // Expected BusinessException because OfflineComServer does not support event registration
    }

    private OfflineComServer createOfflineComServer() {
        return new OfflineComServerImpl(dataModel, outboundComPortProvider, servletBasedInboundComPortProvider, modemBasedInboundComPortProvider, tcpBasedInboundComPortProvider, udpBasedInboundComPortProvider, thesaurus);
    }

    @Test
    public void testOnlineComServer() throws BusinessException {
        OnlineComServer comServer = createOnlineComServer(EVENT_REGISTRATION_URI);

        // Business method
        String url = new EventRegistrationRequestInitiatorImpl(this.engineConfigurationService).getRegistrationURL(comServer);

        // Asserts
        assertThat(url).isEqualTo(EVENT_REGISTRATION_URI);
    }

    @Test(expected = BusinessException.class)
    public void testOnlineComServerWithoutEventRegistrationUri() throws BusinessException {
        OnlineComServer comServer = createOnlineComServer(null);
        comServer.setUsesDefaultEventRegistrationUri(false);

        // Business method
        new EventRegistrationRequestInitiatorImpl(this.engineConfigurationService).getRegistrationURL(comServer);

        // Expected BusinessException because the ComServer explicitly did not support event registration
    }

    @Test
    public void testOnlineComServerByName() throws BusinessException {
        String comServerName = "Online";
        OnlineComServer comServer = createOnlineComServer(EVENT_REGISTRATION_URI);
        when(this.engineConfigurationService.findComServer(comServerName)).thenReturn(Optional.<ComServer>of(comServer));

        // Business method
        String url = new EventRegistrationRequestInitiatorImpl(this.engineConfigurationService).getRegistrationURL(comServerName);

        // Asserts
        assertThat(url).isEqualTo(EVENT_REGISTRATION_URI);
    }

    private OnlineComServer createOnlineComServer(String eventRegistrationUri) {
        final OnlineComServerImpl onlineComServer = new OnlineComServerImpl(dataModel, engineConfigurationService, outboundComPortProvider, servletBasedInboundComPortProvider, modemBasedInboundComPortProvider, tcpBasedInboundComPortProvider, udpBasedInboundComPortProvider, thesaurus);
        onlineComServer.setEventRegistrationUri(eventRegistrationUri);
        return onlineComServer;
    }

    @Test
    public void testRemoteComServer() throws BusinessException {
        RemoteComServer comServer = createRemoteComServerWithRegistrationUri(EVENT_REGISTRATION_URI);

        // Business method
        String url = new EventRegistrationRequestInitiatorImpl(this.engineConfigurationService).getRegistrationURL(comServer);

        // Asserts
        assertThat(url).isEqualTo(EVENT_REGISTRATION_URI);
    }

    @Test(expected = BusinessException.class)
    public void testRemoteComServerWithoutEventRegistrationUri() throws BusinessException {
        RemoteComServer comServer = createRemoteComServerWithRegistrationUri(null);
        comServer.setUsesDefaultEventRegistrationUri(false);

        // Business method
        new EventRegistrationRequestInitiatorImpl(this.engineConfigurationService).getRegistrationURL(comServer);

        // Expected BusinessException because the ComServer explicitly did not support event registration
    }

    @Test
    public void testRemoteComServerByName() throws BusinessException {
        String comServerName = "Remote";
        RemoteComServer comServer = createRemoteComServerWithRegistrationUri(EVENT_REGISTRATION_URI);
        when(this.engineConfigurationService.findComServer(comServerName)).thenReturn(Optional.<ComServer>of(comServer));

        // Business method
        String url = new EventRegistrationRequestInitiatorImpl(this.engineConfigurationService).getRegistrationURL(comServerName);

        // Asserts
        assertThat(url).isEqualTo(EVENT_REGISTRATION_URI);
    }

    private RemoteComServer createRemoteComServerWithRegistrationUri(String eventRegistrationUri) {
        final RemoteComServerImpl remoteComServer = new RemoteComServerImpl(dataModel, outboundComPortProvider, servletBasedInboundComPortProvider, modemBasedInboundComPortProvider, tcpBasedInboundComPortProvider, udpBasedInboundComPortProvider, thesaurus);
        remoteComServer.setEventRegistrationUri(eventRegistrationUri);
        return remoteComServer;
    }

}