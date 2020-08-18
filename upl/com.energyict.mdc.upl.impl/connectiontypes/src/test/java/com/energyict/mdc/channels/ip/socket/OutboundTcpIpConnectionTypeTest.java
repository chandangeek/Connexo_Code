/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.channels.ip.socket;

import com.energyict.mdc.channels.ip.datagrams.OutboundUdpConnectionType;
import com.energyict.mdc.upl.TypedProperties;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;

import com.energyict.protocol.exceptions.ConnectionException;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.time.Duration;

import org.junit.Ignore;
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
@Ignore // this class was only testing java.net.Socket behavior, which is not our business. Also, took quite long - especially testConnectToUnknownHost
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
    public void testTimeoutIsRespected() throws PropertyValidationException {
        OutboundTcpIpConnectionType connectionType = new OutboundTcpIpConnectionType(propertySpecService);

        int timeOut = 1;    // seconds
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
}