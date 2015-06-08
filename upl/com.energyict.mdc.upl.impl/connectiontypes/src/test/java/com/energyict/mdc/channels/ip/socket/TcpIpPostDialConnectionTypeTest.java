package com.energyict.mdc.channels.ip.socket;


import com.energyict.cbo.InvalidValueException;
import com.energyict.cbo.TimeConstants;
import com.energyict.cpo.TypedProperties;
import com.energyict.mdc.protocol.ServerLoggableComChannel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;

import static junit.framework.Assert.assertEquals;
import static org.fest.assertions.api.Assertions.assertThat;
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
    private ServerLoggableComChannel comChannel;

    @Test (expected = InvalidValueException.class)
    public void invalidPostDialTriesTest() throws Exception {
        TcpIpPostDialConnectionType connectionType = new TcpIpPostDialConnectionType();
        TypedProperties properties = TypedProperties.empty();
        properties.setProperty(TcpIpPostDialConnectionType.POST_DIAL_COMMAND, POST_DIAL_COMMAND);
        properties.setProperty(TcpIpPostDialConnectionType.POST_DIAL_TRIES, new BigDecimal(-5));
        properties.setProperty(TcpIpPostDialConnectionType.POST_DIAL_DELAY, POST_DIAL_DELAY);
        connectionType.addProperties(properties);

        // Business method
        try {
        connectionType.sendPostDialCommand(comChannel);
        } catch (InvalidValueException e) {
            assertEquals("XcannotBeEqualOrLessThanZero", e.getMessageId());
            throw e;
        }
    }

    @Test (expected = InvalidValueException.class)
    public void invalidPostDialDelayTest() throws Exception {
        TcpIpPostDialConnectionType connectionType = new TcpIpPostDialConnectionType();
        TypedProperties properties = TypedProperties.empty();
        properties.setProperty(TcpIpPostDialConnectionType.POST_DIAL_COMMAND, POST_DIAL_COMMAND);
        properties.setProperty(TcpIpPostDialConnectionType.POST_DIAL_TRIES, POST_DIAL_TRIES);
        properties.setProperty(TcpIpPostDialConnectionType.POST_DIAL_DELAY, new BigDecimal(-100));
        connectionType.addProperties(properties);

        // Business method
        try {
        connectionType.sendPostDialCommand(comChannel);
        } catch (InvalidValueException e) {
            assertEquals("XcannotBeEqualOrLessThanZero", e.getMessageId());
            throw e;
        }
    }

    @Test
    public void sendPostDialCommandTest() throws Exception {
        TcpIpPostDialConnectionType connectionType = new TcpIpPostDialConnectionType();
        TypedProperties properties = TypedProperties.empty();
        properties.setProperty(TcpIpPostDialConnectionType.POST_DIAL_COMMAND, POST_DIAL_COMMAND);
        properties.setProperty(TcpIpPostDialConnectionType.POST_DIAL_TRIES, POST_DIAL_TRIES);
        properties.setProperty(TcpIpPostDialConnectionType.POST_DIAL_DELAY, POST_DIAL_DELAY);
        connectionType.addProperties(properties);

        long timeBeforePostDial = System.currentTimeMillis();

        // Business method
        connectionType.sendPostDialCommand(comChannel);

        // Asserts
        long timeAfterPostDial = System.currentTimeMillis();
        long durationOfPostDial = timeAfterPostDial - timeBeforePostDial;
        long secs = durationOfPostDial / TimeConstants.MILLISECONDS_IN_SECOND;

        // Asserts
        assertThat(secs).isEqualTo(POST_DIAL_DELAY.intValue() * POST_DIAL_TRIES.intValue() / TimeConstants.MILLISECONDS_IN_SECOND);
        verify(comChannel, times(POST_DIAL_TRIES.intValue())).startWriting();
        verify(comChannel, times(POST_DIAL_TRIES.intValue())).write(POST_DIAL_COMMAND.getBytes());
    }

    @Test
    public void sendPostDialCommandWhenNoPostDialCommandSpecifiedTest() throws Exception {
        TcpIpPostDialConnectionType connectionType = new TcpIpPostDialConnectionType();
        TypedProperties properties = TypedProperties.empty();
        connectionType.addProperties(properties);

        // Business method
        connectionType.sendPostDialCommand(comChannel);

        // Asserts
        verify(comChannel, times(0)).startWriting();
        assertEquals("postDialTries property not specified, thus expecting the default value (1 try)", TcpIpPostDialConnectionType.DEFAULT_POST_DIAL_TRIES, connectionType.getPostDialTriesPropertyValue());
        assertEquals("postDialDelay property not specified, thus expecting the default value (500 ms)", TcpIpPostDialConnectionType.DEFAULT_POST_DIAL_DELAY, connectionType.getPostDialDelayPropertyValue());
    }
}
