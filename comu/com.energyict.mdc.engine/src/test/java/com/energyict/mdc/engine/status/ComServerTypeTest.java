package com.energyict.mdc.engine.status;

import com.energyict.mdc.engine.model.ComServer;
import com.energyict.mdc.engine.model.OfflineComServer;
import com.energyict.mdc.engine.model.OnlineComServer;
import com.energyict.mdc.engine.model.RemoteComServer;

import org.junit.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link ComServerType} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2014-07-18 (11:17)
 */
public class ComServerTypeTest {

    @Test
    public void testOnline () {
        OnlineComServer comServer = mock(OnlineComServer.class);
        when(comServer.isOnline()).thenReturn(true);
        when(comServer.isOffline()).thenReturn(false);
        when(comServer.isRemote()).thenReturn(false);

        // Business method
        ComServerType type = ComServerType.typeFor(comServer);

        // Asserts
        assertThat(type).isEqualTo(ComServerType.ONLINE);
    }

    @Test
    public void testRemote () {
        RemoteComServer comServer = mock(RemoteComServer.class);
        when(comServer.isRemote()).thenReturn(true);
        when(comServer.isOnline()).thenReturn(false);
        when(comServer.isOffline()).thenReturn(false);

        // Business method
        ComServerType type = ComServerType.typeFor(comServer);

        // Asserts
        assertThat(type).isEqualTo(ComServerType.REMOTE);
    }

    @Test
    public void testMobile() {
        OfflineComServer comServer = mock(OfflineComServer.class);
        when(comServer.isOffline()).thenReturn(true);
        when(comServer.isOnline()).thenReturn(false);
        when(comServer.isRemote()).thenReturn(false);

        // Business method
        ComServerType type = ComServerType.typeFor(comServer);

        // Asserts
        assertThat(type).isEqualTo(ComServerType.MOBILE);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testNotSupported() {
        ComServer comServer = mock(ComServer.class);
        when(comServer.isOffline()).thenReturn(false);
        when(comServer.isOnline()).thenReturn(false);
        when(comServer.isRemote()).thenReturn(false);

        // Business method
        ComServerType type = ComServerType.typeFor(comServer);

        // Asserts: see expected exception rule
    }

}