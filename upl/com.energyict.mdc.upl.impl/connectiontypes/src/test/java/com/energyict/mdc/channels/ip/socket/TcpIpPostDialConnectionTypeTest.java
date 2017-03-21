/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.channels.ip.socket;

import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.TypedProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;

import static junit.framework.Assert.assertEquals;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author sva
 * @since 18/03/13 - 15:01
 */
@RunWith(MockitoJUnitRunner.class)
public class TcpIpPostDialConnectionTypeTest {

    private final String POST_DIAL_COMMAND = "postDialCommand";
    private final BigDecimal POST_DIAL_TRIES = new BigDecimal(2);
    private final BigDecimal POST_DIAL_DELAY = new BigDecimal(500);

    @Mock
    private ComChannel comChannel;

    @Mock
    private PropertySpecService propertySpecService;

    @Before
    public void initializeMocksAndFactories() {
        propertySpecService = mock(PropertySpecService.class);
/*
        //TODO
        PropertySpecBuilderWizard.NlsOptions propertySpecBuilder = new PropertySpecBuilderImpl();
        when(propertySpecService.encryptedStringSpec()).thenReturn(propertySpecBuilder);
*/
    }

    @Test(expected = InvalidPropertyException.class)
    public void invalidPostDialTriesTest() throws Exception {
        TcpIpPostDialConnectionType connectionType = new TcpIpPostDialConnectionType(propertySpecService);
        TypedProperties properties = com.energyict.protocolimpl.properties.TypedProperties.empty();
        properties.setProperty(TcpIpPostDialConnectionType.POST_DIAL_COMMAND, POST_DIAL_COMMAND);
        properties.setProperty(TcpIpPostDialConnectionType.POST_DIAL_TRIES, new BigDecimal(-5));
        properties.setProperty(TcpIpPostDialConnectionType.POST_DIAL_DELAY, POST_DIAL_DELAY);

        // Business method
        connectionType.setUPLProperties(properties);
        connectionType.sendPostDialCommand(comChannel);
    }

    @Test(expected = InvalidPropertyException.class)
    public void invalidPostDialDelayTest() throws Exception {
        TcpIpPostDialConnectionType connectionType = new TcpIpPostDialConnectionType(propertySpecService);
        TypedProperties properties = com.energyict.protocolimpl.properties.TypedProperties.empty();
        properties.setProperty(TcpIpPostDialConnectionType.POST_DIAL_COMMAND, POST_DIAL_COMMAND);
        properties.setProperty(TcpIpPostDialConnectionType.POST_DIAL_TRIES, POST_DIAL_TRIES);
        properties.setProperty(TcpIpPostDialConnectionType.POST_DIAL_DELAY, new BigDecimal(-100));

        // Business method
        connectionType.setUPLProperties(properties);
        connectionType.sendPostDialCommand(comChannel);
    }

    @Test
    public void sendPostDialCommandTest() throws Exception {
        TcpIpPostDialConnectionType connectionType = new TcpIpPostDialConnectionType(propertySpecService);
        TypedProperties properties = com.energyict.protocolimpl.properties.TypedProperties.empty();
        properties.setProperty(TcpIpPostDialConnectionType.POST_DIAL_COMMAND, POST_DIAL_COMMAND);
        properties.setProperty(TcpIpPostDialConnectionType.POST_DIAL_TRIES, POST_DIAL_TRIES);
        properties.setProperty(TcpIpPostDialConnectionType.POST_DIAL_DELAY, POST_DIAL_DELAY);
        connectionType.setUPLProperties(properties);

        long timeBeforePostDial = System.currentTimeMillis();

        // Business method
        connectionType.sendPostDialCommand(comChannel);

        // Asserts
        long timeAfterPostDial = System.currentTimeMillis();
        long durationOfPostDial = timeAfterPostDial - timeBeforePostDial;
        long secs = durationOfPostDial / 1000;

        // Asserts
        assertThat(secs).isEqualTo(POST_DIAL_DELAY.intValue() * POST_DIAL_TRIES.intValue() / 1000);
        verify(comChannel, times(POST_DIAL_TRIES.intValue())).startWriting();
        verify(comChannel, times(POST_DIAL_TRIES.intValue())).write(POST_DIAL_COMMAND.getBytes());
    }

    @Test
    public void sendPostDialCommandWhenNoPostDialCommandSpecifiedTest() throws Exception {
        TcpIpPostDialConnectionType connectionType = new TcpIpPostDialConnectionType(propertySpecService);
        TypedProperties properties = com.energyict.protocolimpl.properties.TypedProperties.empty();
        connectionType.setUPLProperties(properties);

        // Business method
        connectionType.sendPostDialCommand(comChannel);

        // Asserts
        verify(comChannel, times(0)).startWriting();
        assertEquals("postDialTries property not specified, thus expecting the default value (1 try)", TcpIpPostDialConnectionType.DEFAULT_POST_DIAL_TRIES, connectionType.getPostDialTriesPropertyValue());
        assertEquals("postDialDelay property not specified, thus expecting the default value (500 ms)", TcpIpPostDialConnectionType.DEFAULT_POST_DIAL_DELAY, connectionType.getPostDialDelayPropertyValue());
    }
}
