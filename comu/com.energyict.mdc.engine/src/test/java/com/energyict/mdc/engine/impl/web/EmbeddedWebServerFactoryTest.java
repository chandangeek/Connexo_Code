package com.energyict.mdc.engine.impl.web;

import com.energyict.comserver.exceptions.CodingException;
import org.fest.assertions.api.Assertions;

import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.engine.impl.web.DefaultEmbeddedWebServerFactory;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServer;
import com.energyict.mdc.engine.impl.web.EmbeddedWebServerFactory;
import com.energyict.mdc.engine.model.OfflineComServer;
import com.energyict.mdc.engine.model.impl.OfflineComServerImpl;
import com.energyict.mdc.engine.model.impl.OnlineComServerImpl;
import com.energyict.mdc.engine.model.impl.RemoteComServerImpl;
import org.junit.Test;

import java.net.URISyntaxException;

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
public class EmbeddedWebServerFactoryTest {

    private static final String EVENT_REGISTRATION_URL = "ws://comserver.energyict.com/events/registration";
    private static final String INVALID_URI = "Anything but a valid URL";

    @Test
    public void testEventsWithOfflineComServer () throws BusinessException {
        OfflineComServer comServer = mock(OfflineComServerImpl.class);
        doCallRealMethod().when(comServer).getEventRegistrationUriIfSupported();

        // Business method
        EmbeddedWebServer eventWebServer = EmbeddedWebServerFactory.DEFAULT.get().findOrCreateEventWebServer(comServer);

        // Asserts
        assertThat(eventWebServer).isNotNull();
        assertThat(eventWebServer.getClass().getSimpleName()).startsWith("Void");
    }

    @Test
    public void testEventsWithOnlineComServerThatDoesNotSupportEventRegistration () throws BusinessException {
        ServerOnlineComServer comServer = mock(OnlineComServerImpl.class);
        doCallRealMethod().when(comServer).getEventRegistrationUriIfSupported();
        doCallRealMethod().when(comServer).isOffline();
        doCallRealMethod().when(comServer).isOnline();
        doCallRealMethod().when(comServer).isRemote();
        when(comServer.getEventRegistrationUri()).thenReturn(null);

        // Business method
        EmbeddedWebServer eventWebServer = EmbeddedWebServerFactory.DEFAULT.get().findOrCreateEventWebServer(comServer);

        // Asserts
        assertThat(eventWebServer).isNotNull();
        assertThat(eventWebServer.getClass().getSimpleName()).startsWith("Void");
    }

    @Test(expected = CodingException.class)
    public void testEventsWithOnlineComServerWithInvalidEventRegistrationURI () throws BusinessException {
        ServerOnlineComServer comServer = mock(OnlineComServerImpl.class);
        doCallRealMethod().when(comServer).getEventRegistrationUriIfSupported();
        doCallRealMethod().when(comServer).isOffline();
        doCallRealMethod().when(comServer).isOnline();
        doCallRealMethod().when(comServer).isRemote();
        when(comServer.getEventRegistrationUri()).thenReturn(INVALID_URI);

        // Business method
        try {
            EmbeddedWebServerFactory.DEFAULT.get().findOrCreateEventWebServer(comServer);
        }
        catch (CodingException e) {
            Assertions.assertThat(e.getCause()).isInstanceOf(URISyntaxException.class);
            throw e;
        }
    }

    @Test
    public void testEventsWithOnlineComServer () throws BusinessException {
        ServerOnlineComServer comServer = mock(OnlineComServerImpl.class);
        doCallRealMethod().when(comServer).getEventRegistrationUriIfSupported();
        doCallRealMethod().when(comServer).isOffline();
        doCallRealMethod().when(comServer).isOnline();
        doCallRealMethod().when(comServer).isRemote();
        when(comServer.getEventRegistrationUri()).thenReturn(EVENT_REGISTRATION_URL);

        // Business method
        EmbeddedWebServer eventWebServer = EmbeddedWebServerFactory.DEFAULT.get().findOrCreateEventWebServer(comServer);

        // Asserts
        assertThat(eventWebServer).isNotNull();
    }

    @Test
    public void testEventsWithRemoteComServerThatDoesNotSupportEventRegistration () throws BusinessException {
        ServerRemoteComServer comServer = mock(RemoteComServerImpl.class);
        doCallRealMethod().when(comServer).getEventRegistrationUriIfSupported();
        doCallRealMethod().when(comServer).isOffline();
        doCallRealMethod().when(comServer).isOnline();
        doCallRealMethod().when(comServer).isRemote();
        when(comServer.getEventRegistrationUri()).thenReturn(null);

        // Business method
        EmbeddedWebServer eventWebServer = EmbeddedWebServerFactory.DEFAULT.get().findOrCreateEventWebServer(comServer);

        // Asserts
        assertThat(eventWebServer).isNotNull();
        assertThat(eventWebServer.getClass().getSimpleName()).startsWith("Void");
    }

    @Test(expected = CodingException.class)
    public void testEventsWithRemoteComServerWithInvalidEventRegistrationURL () throws BusinessException {
        ServerRemoteComServer comServer = mock(RemoteComServerImpl.class);
        doCallRealMethod().when(comServer).getEventRegistrationUriIfSupported();
        doCallRealMethod().when(comServer).isOffline();
        doCallRealMethod().when(comServer).isOnline();
        doCallRealMethod().when(comServer).isRemote();
        when(comServer.getEventRegistrationUri()).thenReturn(INVALID_URI);

        // Business method
        try {
            EmbeddedWebServerFactory.DEFAULT.get().findOrCreateEventWebServer(comServer);
        }
        catch (CodingException e) {
            Assertions.assertThat(e.getCause()).isInstanceOf(URISyntaxException.class);
            throw e;
        }
    }

    @Test
    public void testEventsWithRemoteComServer () throws BusinessException {
        ServerRemoteComServer comServer = mock(RemoteComServerImpl.class);
        doCallRealMethod().when(comServer).getEventRegistrationUriIfSupported();
        doCallRealMethod().when(comServer).isOffline();
        doCallRealMethod().when(comServer).isOnline();
        doCallRealMethod().when(comServer).isRemote();
        when(comServer.getEventRegistrationUri()).thenReturn(EVENT_REGISTRATION_URL);

        // Business method
        EmbeddedWebServer eventWebServer = EmbeddedWebServerFactory.DEFAULT.get().findOrCreateEventWebServer(comServer);

        // Asserts
        assertThat(eventWebServer).isNotNull();
    }

    @Test
    public void testQueriesWithOnlineComServer () throws BusinessException {
        ServerOnlineComServer comServer = mock(OnlineComServerImpl.class);
        when(comServer.getQueryApiPostUri()).thenReturn("http://localhost/remote/query-api");
        doCallRealMethod().when(comServer).getQueryApiPostUriIfSupported();

        // Business method
        EmbeddedWebServer eventWebServer = EmbeddedWebServerFactory.DEFAULT.get().findOrCreateRemoteQueryWebServer(comServer);

        // Asserts
        assertThat(eventWebServer).isNotNull();
        assertThat(eventWebServer.getClass().getSimpleName()).doesNotMatch("Void.*");
    }

    @Test(expected = CodingException.class)
    public void testQueriesWithURISyntaxError () throws BusinessException {
        ServerOnlineComServer comServer = mock(OnlineComServerImpl.class);
        when(comServer.getQueryApiPostUri()).thenReturn(INVALID_URI);
        doCallRealMethod().when(comServer).getQueryApiPostUriIfSupported();

        // Business method
        try {
            EmbeddedWebServerFactory.DEFAULT.get().findOrCreateRemoteQueryWebServer(comServer);
        }
        catch (CodingException e) {
            Assertions.assertThat(e.getCause()).isInstanceOf(URISyntaxException.class);
            throw e;
        }
    }

    @Test
    public void testQueriesWithOnlineComServerThatDoesNotSupportRemoteQueries () throws BusinessException {
        ServerOnlineComServer comServer = mock(OnlineComServerImpl.class);
        doThrow(BusinessException.class).when(comServer).getQueryApiPostUriIfSupported();

        // Business method
        EmbeddedWebServer eventWebServer = EmbeddedWebServerFactory.DEFAULT.get().findOrCreateRemoteQueryWebServer(comServer);

        // Asserts
        assertThat(eventWebServer).isNotNull();
        assertThat(eventWebServer.getClass().getSimpleName()).startsWith("Void");
    }

}