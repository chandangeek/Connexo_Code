/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.channels.ip.socket;

import com.energyict.mdc.channels.ip.OutboundIpConnectionType;
import com.energyict.mdc.channels.ip.datagrams.OutboundUdpConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.energyict.protocol.exceptions.ConnectionException;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.Duration;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests the {@link OutboundTcpIpConnectionType} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-24 (16:03)
 */
@RunWith(MockitoJUnitRunner.class)
public class OutboundTcpIpConnectionTypeTest {

    private static final int DEFAULT_HTTP_PORT = 8080;

    @Mock
    private PropertySpecService propertySpecService;

    @Test(expected = UnknownHostException.class)
    public void testConnectToUnknownHost() throws Throwable {

        com.energyict.mdc.upl.properties.TypedProperties properties = getConnectionProperties("www.ditiszekereendomeinnaamdienietbestaat.zelfsdeextentiebestaatniet", DEFAULT_HTTP_PORT, 1);
        OutboundTcpIpConnectionType connectionType = new OutboundTcpIpConnectionType(this.propertySpecService);
        connectionType.setUPLProperties(properties);

        try {
            // Business method
            connectionType.connect();
        } catch (ConnectionException e) {
            // Expected a ConnectionException but want to assert that the cause is a SocketTimeoutException
            Throwable cause = e.getCause();
            assertThat(cause).isInstanceOf(UnknownHostException.class);
            throw cause;
        }
    }

    @Test
    public void testTimeoutIsRespected() throws SocketTimeoutException, PropertyValidationException {
        OutboundTcpIpConnectionType connectionType = new OutboundTcpIpConnectionType(propertySpecService);

        int timeOut = 3;    // seconds
        com.energyict.mdc.upl.properties.TypedProperties typedProperties = getConnectionProperties("10.0.13.13", DEFAULT_HTTP_PORT, timeOut);
        long timeBeforeConnect = System.currentTimeMillis();
        connectionType.setUPLProperties(typedProperties);

        try {
            // Business method
            connectionType.connect();
        } catch (ConnectionException e) {
            long timeAfterConnect = System.currentTimeMillis();
            long connectTime = timeAfterConnect - timeBeforeConnect;
            long secs = connectTime / 1000;
            assertThat(e.getCause()).isInstanceOf(SocketTimeoutException.class);

            // Asserts
            assertThat(secs).isEqualTo(timeOut);
        }
    }

    private com.energyict.mdc.upl.properties.TypedProperties getConnectionProperties(String hostName, int port, int timeOut) {
        TypedProperties typedProperties = TypedProperties.empty();

        typedProperties.setProperty(OutboundUdpConnectionType.HOST_PROPERTY_NAME, hostName);
        typedProperties.setProperty(OutboundUdpConnectionType.PORT_PROPERTY_NAME, new BigDecimal(port));
        typedProperties.setProperty(OutboundUdpConnectionType.CONNECTION_TIMEOUT_PROPERTY_NAME, Duration.ofSeconds(timeOut));

        return typedProperties;
    }

    @Test
    public void testConnectToHost() throws ConnectionException, PropertyValidationException {
        OutboundTcpIpConnectionType connectionType = this.newTcpIpConnectionType();

        ComChannel comChannel = null;
        try {
            // Business method
            comChannel = connectionType.connect();

            // Asserts
            assertThat(comChannel).isNotNull();
        } finally {
            this.close(comChannel);
        }
    }

    @Test
    public void testTalkToHost() throws ConnectionException, PropertyValidationException {
        OutboundTcpIpConnectionType connectionType = this.newTcpIpConnectionType();

        ComChannel comChannel = null;
        try {
            // Business methods
            comChannel = connectionType.connect();
            comChannel.write("GET / HTTP/1.1\r\n\r\n".getBytes());
            comChannel.startReading();
            byte[] buffer = new byte[100];
            int bytesRead = comChannel.read(buffer);

            // Asserts
            assertThat(bytesRead).isGreaterThan(0);
        } finally {
            this.close(comChannel);
        }
    }

    private OutboundTcpIpConnectionType newTcpIpConnectionType() throws PropertyValidationException {
        OutboundTcpIpConnectionType connectionType = new OutboundTcpIpConnectionType(propertySpecService);
        TypedProperties properties = TypedProperties.empty();
        properties.setProperty(OutboundIpConnectionType.HOST_PROPERTY_NAME, "jenkins.eict.local");
        properties.setProperty(OutboundIpConnectionType.PORT_PROPERTY_NAME, new BigDecimal(DEFAULT_HTTP_PORT));
        connectionType.setUPLProperties(properties);
        return connectionType;
    }

    private void close(ComChannel comChannel) {
        if (comChannel != null) {
            comChannel.close();
        }
    }
}
