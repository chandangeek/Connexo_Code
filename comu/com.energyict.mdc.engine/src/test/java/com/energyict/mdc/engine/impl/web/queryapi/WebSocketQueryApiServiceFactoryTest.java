package com.energyict.mdc.engine.impl.web.queryapi;

import org.fest.assertions.api.Assertions;

import org.junit.*;

import static org.mockito.Mockito.mock;

/**
 * Tests the {@link WebSocketQueryApiServiceFactory} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2013-04-10 (16:30)
 */
public class WebSocketQueryApiServiceFactoryTest {

    @After
    public void resetFactory () {
        WebSocketQueryApiServiceFactory.setInstance(null);
    }

    @Test
    public void testFactoryReturnsDefaultIfNotInitializedYet () {
        Assertions.assertThat(WebSocketQueryApiServiceFactory.getInstance()).isNotNull();
    }

    @Test
    public void testFactoryReturnsWhatWasInitialized () {
        WebSocketQueryApiServiceFactory factory = mock(WebSocketQueryApiServiceFactory.class);
        WebSocketQueryApiServiceFactory.setInstance(factory);

        Assertions.assertThat(WebSocketQueryApiServiceFactory.getInstance()).isSameAs(factory);
    }

}