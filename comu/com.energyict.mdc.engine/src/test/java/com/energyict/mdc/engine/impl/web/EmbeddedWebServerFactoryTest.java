package com.energyict.mdc.engine.impl.web;

import com.elster.jupiter.nls.Thesaurus;
import com.elster.jupiter.orm.DataModel;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.engine.config.*;
import com.energyict.mdc.engine.exceptions.CodingException;
import com.energyict.mdc.engine.impl.core.RunningOnlineComServer;
import com.energyict.mdc.engine.config.impl.OfflineComServerImpl;
import com.energyict.mdc.engine.config.impl.OnlineComServerImpl;
import com.energyict.mdc.engine.config.impl.RemoteComServerImpl;
import com.energyict.mdc.engine.impl.web.events.WebSocketEventPublisherFactory;

import com.google.inject.Provider;
import org.fest.assertions.api.Assertions;

import java.net.URISyntaxException;

import org.junit.*;
import org.junit.runner.*;

import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link DefaultEmbeddedWebServerFactory} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (16:57)
 */
@RunWith(MockitoJUnitRunner.class)
public class EmbeddedWebServerFactoryTest {

    private static final String EVENT_REGISTRATION_URL = "ws://comserver.energyict.com/events/registration";
    private static final String INVALID_URI = "Anything but a valid URL";

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
    @Mock
    private WebSocketEventPublisherFactory webSocketEventPublisherFactory;

    private EmbeddedWebServerFactory factory;

    @Before
    public void setupFactoryUnderTest () {
        this.factory = new DefaultEmbeddedWebServerFactory(this.webSocketEventPublisherFactory);
    }

    private OfflineComServer createOfflineComServer() {
        return new OfflineComServerImpl(dataModel, outboundComPortProvider, servletBasedInboundComPortProvider, modemBasedInboundComPortProvider, tcpBasedInboundComPortProvider, udpBasedInboundComPortProvider, thesaurus);
    }

    @Test
    public void testEventsWithOfflineComServer () throws BusinessException {
        OfflineComServer comServer = createOfflineComServer();

        // Business method
        EmbeddedWebServer eventWebServer = this.factory.findOrCreateEventWebServer(comServer);

        // Asserts
        assertThat(eventWebServer).isNotNull();
        assertThat(eventWebServer.getClass().getSimpleName()).startsWith("Void");
    }


    private OnlineComServer createOnlineComServer(String eventRegistrationUri) {
        final OnlineComServerImpl onlineComServer = new OnlineComServerImpl(dataModel, engineConfigurationService, outboundComPortProvider, servletBasedInboundComPortProvider, modemBasedInboundComPortProvider, tcpBasedInboundComPortProvider, udpBasedInboundComPortProvider, thesaurus);
        onlineComServer.setEventRegistrationUri(eventRegistrationUri);
        return onlineComServer;
    }

    @Test
    public void testEventsWithOnlineComServerThatDoesNotSupportEventRegistration () throws BusinessException {
        OnlineComServer comServer = createOnlineComServer(null);
        comServer.setUsesDefaultEventRegistrationUri(false);

        // Business method
        EmbeddedWebServer eventWebServer = this.factory.findOrCreateEventWebServer(comServer);

        // Asserts
        assertThat(eventWebServer).isNotNull();
        assertThat(eventWebServer.getClass().getSimpleName()).startsWith("Void");
    }

    @Test(expected = CodingException.class)
    public void testEventsWithOnlineComServerWithInvalidEventRegistrationURI () throws BusinessException {
        OnlineComServer comServer = createOnlineComServer(INVALID_URI);

        // Business method
        try {
            this.factory.findOrCreateEventWebServer(comServer);
        }
        catch (CodingException e) {
            Assertions.assertThat(e.getCause()).isInstanceOf(URISyntaxException.class);
            throw e;
        }
    }

    @Test
    public void testEventsWithOnlineComServer () throws BusinessException {
        OnlineComServer comServer = createOnlineComServer(EVENT_REGISTRATION_URL);

        // Business method
        EmbeddedWebServer eventWebServer = this.factory.findOrCreateEventWebServer(comServer);

        // Asserts
        assertThat(eventWebServer).isNotNull();
    }

    private RemoteComServer createRemoteComServerWithRegistrationUri(String eventRegistrationUri) {
        final RemoteComServerImpl remoteComServer = new RemoteComServerImpl(dataModel, outboundComPortProvider, servletBasedInboundComPortProvider, modemBasedInboundComPortProvider, tcpBasedInboundComPortProvider, udpBasedInboundComPortProvider, thesaurus);
        remoteComServer.setEventRegistrationUri(eventRegistrationUri);
        return remoteComServer;
    }

    @Test
    public void testEventsWithRemoteComServerThatDoesNotSupportEventRegistration () throws BusinessException {
        RemoteComServer comServer = createRemoteComServerWithRegistrationUri(null);
        comServer.setUsesDefaultEventRegistrationUri(false);

        // Business method
        EmbeddedWebServer eventWebServer = this.factory.findOrCreateEventWebServer(comServer);

        // Asserts
        assertThat(eventWebServer).isNotNull();
        assertThat(eventWebServer.getClass().getSimpleName()).startsWith("Void");
    }

    @Test(expected = CodingException.class)
    public void testEventsWithRemoteComServerWithInvalidEventRegistrationURL () throws BusinessException {
        RemoteComServer comServer = createRemoteComServerWithRegistrationUri(INVALID_URI);

        // Business method
        try {
            this.factory.findOrCreateEventWebServer(comServer);
        }
        catch (CodingException e) {
            Assertions.assertThat(e.getCause()).isInstanceOf(URISyntaxException.class);
            throw e;
        }
    }

    @Test
    public void testEventsWithRemoteComServer () throws BusinessException {
        RemoteComServer comServer = createRemoteComServerWithRegistrationUri(EVENT_REGISTRATION_URL);

        // Business method
        EmbeddedWebServer eventWebServer = this.factory.findOrCreateEventWebServer(comServer);

        // Asserts
        assertThat(eventWebServer).isNotNull();
    }

    @Test
    public void testQueriesWithOnlineComServer () throws BusinessException {
        OnlineComServer comServer = createOnlineComServer(null);
        comServer.setQueryAPIPostUri("http://localhost/remote/query-api");
        RunningOnlineComServer runningOnlineComServer = mock(RunningOnlineComServer.class);
        when(runningOnlineComServer.getComServer()).thenReturn(comServer);

        // Business method
        EmbeddedWebServer eventWebServer = this.factory.findOrCreateRemoteQueryWebServer(runningOnlineComServer);

        // Asserts
        assertThat(eventWebServer).isNotNull();
        assertThat(eventWebServer.getClass().getSimpleName()).doesNotMatch("Void.*");
    }

    @Test(expected = CodingException.class)
    public void testQueriesWithURISyntaxError () throws BusinessException {
        OnlineComServer comServer = createOnlineComServer(null);
        comServer.setQueryAPIPostUri(INVALID_URI);
        RunningOnlineComServer runningOnlineComServer = mock(RunningOnlineComServer.class);
        when(runningOnlineComServer.getComServer()).thenReturn(comServer);

        // Business method
        try {
            this.factory.findOrCreateRemoteQueryWebServer(runningOnlineComServer);
        }
        catch (CodingException e) {
            Assertions.assertThat(e.getCause()).isInstanceOf(URISyntaxException.class);
            throw e;
        }
    }

    @Test
    public void testQueriesWithOnlineComServerThatDoesNotSupportRemoteQueries () throws BusinessException {
        OnlineComServer comServer = mock(OnlineComServer.class);
        doThrow(BusinessException.class).when(comServer).getQueryApiPostUriIfSupported();
        RunningOnlineComServer runningOnlineComServer = mock(RunningOnlineComServer.class);
        when(runningOnlineComServer.getComServer()).thenReturn(comServer);

        // Business method
        EmbeddedWebServer eventWebServer = this.factory.findOrCreateRemoteQueryWebServer(runningOnlineComServer);

        // Asserts
        assertThat(eventWebServer).isNotNull();
        assertThat(eventWebServer.getClass().getSimpleName()).startsWith("Void");
    }

}