package com.energyict.mdc.engine.impl.events.registration;

import com.energyict.mdc.ManagerFactory;
import com.energyict.mdc.ServerManager;
import com.energyict.mdc.common.BusinessException;
import com.energyict.mdc.engine.impl.events.registration.EventRegistrationRequestInitiatorImpl;
import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OfflineComServer;
import com.energyict.mdc.engine.model.impl.OfflineComServerImpl;
import com.energyict.mdc.engine.model.impl.OnlineComServerImpl;
import com.energyict.mdc.engine.model.impl.RemoteComServerImpl;
import com.energyict.mdc.servers.ComServerFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.mdc.engine.impl.events.registration.EventRegistrationRequestInitiatorImpl} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-11-02 (15:32)
 */
@RunWith(MockitoJUnitRunner.class)
public class EventRegistrationRequestInitiatorImplTest {

    private static final String EVENT_REGISTRATION_URI = "ws://comserver.energyict.com/events/registration";

    @Mock
    private ServerManager manager;
    @Mock
    private ComServerFactory comServerFactory;

    @Before
    public void initializeMocksAndFactories () {
        ManagerFactory.setCurrent(this.manager);
        when(this.manager.getComServerFactory()).thenReturn(this.comServerFactory);
    }

    @Test(expected = BusinessException.class)
    public void testNonExistingComServer () throws BusinessException {
        String comServerName = "Does not exist";
        when(this.comServerFactory.findBySystemName(comServerName)).thenReturn(null);

        // Business method
        new EventRegistrationRequestInitiatorImpl().getRegistrationURL(comServerName);

        // Expected BusinessException because the ComServer does not exist
    }

    @Test(expected = BusinessException.class)
    public void testOfflineComServerByName () throws BusinessException {
        String comServerName = "Offline";
        OfflineComServer comServer = mock(OfflineComServerImpl.class);
        doCallRealMethod().when(comServer).getEventRegistrationUriIfSupported();
        when(this.comServerFactory.findBySystemName(comServerName)).thenReturn((ComServer)comServer);

        // Business method
        new EventRegistrationRequestInitiatorImpl().getRegistrationURL(comServerName);

        // Expected BusinessException because OfflineComServer does not support event registration
    }

    @Test(expected = BusinessException.class)
    public void testOfflineComServer () throws BusinessException {
        OfflineComServer comServer = mock(OfflineComServerImpl.class);
        doCallRealMethod().when(comServer).getEventRegistrationUriIfSupported();

        // Business method
        new EventRegistrationRequestInitiatorImpl().getRegistrationURL(comServer);

        // Expected BusinessException because OfflineComServer does not support event registration
    }

    @Test
    public void testOnlineComServer () throws BusinessException {
        ServerOnlineComServer comServer = mock(OnlineComServerImpl.class);
        doCallRealMethod().when(comServer).getEventRegistrationUriIfSupported();
        when(comServer.getEventRegistrationUri()).thenReturn(EVENT_REGISTRATION_URI);

        // Business method
        String url = new EventRegistrationRequestInitiatorImpl().getRegistrationURL(comServer);

        // Asserts
        Assertions.assertThat(url).isEqualTo(EVENT_REGISTRATION_URI);
    }

    @Test(expected = BusinessException.class)
    public void testOnlineComServerWithoutEventRegistrationUri () throws BusinessException {
        ServerOnlineComServer comServer = mock(OnlineComServerImpl.class);
        doCallRealMethod().when(comServer).getEventRegistrationUriIfSupported();
        when(comServer.getEventRegistrationUri()).thenReturn(null);

        // Business method
        new EventRegistrationRequestInitiatorImpl().getRegistrationURL(comServer);

        // Expected BusinessException because the ComServer explicitly did not support event registration
    }

    @Test
    public void testOnlineComServerByName () throws BusinessException {
        String comServerName = "Online";
        ServerOnlineComServer comServer = mock(OnlineComServerImpl.class);
        doCallRealMethod().when(comServer).getEventRegistrationUriIfSupported();
        when(comServer.getEventRegistrationUri()).thenReturn(EVENT_REGISTRATION_URI);
        when(this.comServerFactory.findBySystemName(comServerName)).thenReturn((ComServer)comServer);

        // Business method
        String url = new EventRegistrationRequestInitiatorImpl().getRegistrationURL(comServer);

        // Asserts
        Assertions.assertThat(url).isEqualTo(EVENT_REGISTRATION_URI);
    }

    @Test
    public void testRemoteComServer () throws BusinessException {
        ServerRemoteComServer comServer = mock(RemoteComServerImpl.class);
        doCallRealMethod().when(comServer).getEventRegistrationUriIfSupported();
        when(comServer.getEventRegistrationUri()).thenReturn(EVENT_REGISTRATION_URI);

        // Business method
        String url = new EventRegistrationRequestInitiatorImpl().getRegistrationURL(comServer);

        // Asserts
        Assertions.assertThat(url).isEqualTo(EVENT_REGISTRATION_URI);
    }

    @Test(expected = BusinessException.class)
    public void testRemoteComServerWithoutEventRegistrationUri () throws BusinessException {
        ServerRemoteComServer comServer = mock(RemoteComServerImpl.class);
        doCallRealMethod().when(comServer).getEventRegistrationUriIfSupported();
        when(comServer.getEventRegistrationUri()).thenReturn(null);

        // Business method
        new EventRegistrationRequestInitiatorImpl().getRegistrationURL(comServer);

        // Expected BusinessException because the ComServer explicitly did not support event registration
    }

    @Test
    public void testRemoteComServerByName () throws BusinessException {
        String comServerName = "Remote";
        ServerRemoteComServer comServer = mock(RemoteComServerImpl.class);
        doCallRealMethod().when(comServer).getEventRegistrationUriIfSupported();
        when(comServer.getEventRegistrationUri()).thenReturn(EVENT_REGISTRATION_URI);
        when(this.comServerFactory.findBySystemName(comServerName)).thenReturn((ComServer)comServer);

        // Business method
        String url = new EventRegistrationRequestInitiatorImpl().getRegistrationURL(comServer);

        // Asserts
        Assertions.assertThat(url).isEqualTo(EVENT_REGISTRATION_URI);
    }

}