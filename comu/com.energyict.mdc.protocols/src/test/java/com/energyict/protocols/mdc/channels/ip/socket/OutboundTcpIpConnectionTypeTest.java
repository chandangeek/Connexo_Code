package com.energyict.protocols.mdc.channels.ip.socket;

import com.energyict.cbo.TimeConstants;
import com.energyict.mdc.common.TimeDuration;
import com.energyict.comserver.monitor.ComServerMonitorImplMBean;
import com.energyict.comserver.monitor.EventAPIStatistics;
import com.energyict.comserver.monitor.ManagementBeanFactory;
import com.energyict.comserver.monitorimpl.ComServerMonitor;
import com.energyict.comserver.monitorimpl.ManagementBeanFactoryImpl;
import com.energyict.comserver.scheduling.RunningComServer;
import com.energyict.mdc.common.TypedProperties;
import com.energyict.mdc.ports.ComPort;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.ConnectionException;
import com.energyict.mdc.tasks.ConnectionTaskProperty;
import com.energyict.mdc.tasks.ConnectionTaskPropertyImpl;
import com.energyict.protocols.mdc.channels.ip.OutboundIpConnectionType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link com.energyict.protocols.mdc.channels.ip.socket.OutboundTcpIpConnectionType} component.
 *
 * @author Rudi Vankeirsbilck (rudi)
 * @since 2012-07-24 (16:03)
 */
@RunWith(MockitoJUnitRunner.class)
public class OutboundTcpIpConnectionTypeTest {

    private static final int DEFAULT_HTTP_PORT = 8080;

    @Mock
    private ManagementBeanFactory managementBeanFactory;
    @Mock(extraInterfaces = ComServerMonitor.class)
    private ComServerMonitorImplMBean comServerMonitor;
    @Mock
    private EventAPIStatistics eventAPIStatistics;

    @Before
    public void initializeMocksAndFactories () {
        ManagementBeanFactoryImpl.setInstance(managementBeanFactory);
        when(managementBeanFactory.findOrCreateFor(any(RunningComServer.class))).thenReturn(comServerMonitor);
        when(((ComServerMonitor)comServerMonitor).getEventApiStatistics()).thenReturn(eventAPIStatistics);
    }
    @Test(expected = UnknownHostException.class)
    public void testConnectToUnknownHost () throws Throwable {
        OutboundTcpIpConnectionType connectionType = new OutboundTcpIpConnectionType();
        ComPort comPort = getMockedComPort();

        ArrayList<ConnectionTaskProperty> properties = getConnectionProperties("www.ditiszekereendomeinnaamdienietbestaat.zelfsdeextentiebestaatniet", DEFAULT_HTTP_PORT, 1);

        try {
            // Business method
            connectionType.connect(properties);
        }
        catch (ConnectionException e) {
            // Expected a ConnectionException but want to assert that the cause is a SocketTimeoutException
            Throwable cause = e.getCause();
            assertThat(cause).isInstanceOf(UnknownHostException.class);
            throw cause;
        }
    }

    @Test
    public void testTimeoutIsRespected () throws SocketTimeoutException {
        OutboundTcpIpConnectionType connectionType = new OutboundTcpIpConnectionType();
        ComPort comPort = getMockedComPort();

        int timeOut = 1;    // seconds
        ArrayList<ConnectionTaskProperty> properties = getConnectionProperties("10.0.13.13", DEFAULT_HTTP_PORT, timeOut);
        long timeBeforeConnect = System.currentTimeMillis();

        try {
            // Business method
            connectionType.connect(properties);
        }
        catch (ConnectionException e) {
            long timeAfterConnect = System.currentTimeMillis();
            long connectTime = timeAfterConnect - timeBeforeConnect;
            long secs = connectTime / TimeConstants.MILLISECONDS_IN_SECOND;
            assertThat(e.getCause()).isInstanceOf(SocketTimeoutException.class);

            // Asserts
            assertThat(secs).isEqualTo(timeOut);
        }
    }

    private ArrayList<ConnectionTaskProperty> getConnectionProperties (String hostName, int port, int timeOut) {
        ArrayList<ConnectionTaskProperty> properties = new ArrayList<>(0);
        ConnectionTaskPropertyImpl hostProperty = new ConnectionTaskPropertyImpl(OutboundIpConnectionType.HOST_PROPERTY_NAME);
        hostProperty.setValue(hostName);
        properties.add(hostProperty);
        ConnectionTaskPropertyImpl portProperty = new ConnectionTaskPropertyImpl(OutboundIpConnectionType.PORT_PROPERTY_NAME);
        portProperty.setValue(new BigDecimal(port));
        properties.add(portProperty);
        ConnectionTaskPropertyImpl timeOutProperty = new ConnectionTaskPropertyImpl(OutboundIpConnectionType.CONNECTION_TIMEOUT_PROPERTY_NAME);
        timeOutProperty.setValue(new TimeDuration(timeOut));
        properties.add(timeOutProperty);
        return properties;
    }

    private ComPort getMockedComPort() {
        return mock(ComPort.class);
    }

    @Test
    public void testConnectToJira () throws ConnectionException {
        OutboundTcpIpConnectionType connectionType = this.newTcpIpConnectionType();

        ComChannel comChannel = null;
        try {
            // Business method
            comChannel = connectionType.connect(new ArrayList<ConnectionTaskProperty>(0));

            // Asserts
            assertThat(comChannel).isNotNull();
        }
        finally {
            this.close(comChannel);
        }
    }

    @Test
    public void testTalkToJira () throws ConnectionException {
        OutboundTcpIpConnectionType connectionType = this.newTcpIpConnectionType();

        ComChannel comChannel = null;
        try {
            // Business methods
            comChannel = connectionType.connect(new ArrayList<ConnectionTaskProperty>(0));
            comChannel.write("GET / HTTP/1.1\r\n\r\n".getBytes());
            comChannel.startReading();
            byte[] buffer = new byte[100];
            int bytesRead = comChannel.read(buffer);

            // Asserts
            assertThat(bytesRead).isGreaterThan(0);
        }
        finally {
            this.close(comChannel);
        }
    }

    private OutboundTcpIpConnectionType newTcpIpConnectionType () {
        OutboundTcpIpConnectionType connectionType = new OutboundTcpIpConnectionType();
        TypedProperties properties = TypedProperties.empty();
        properties.setProperty(OutboundIpConnectionType.HOST_PROPERTY_NAME, "jira.eict.vpdc");
        properties.setProperty(OutboundIpConnectionType.PORT_PROPERTY_NAME, new BigDecimal(DEFAULT_HTTP_PORT));
        connectionType.addProperties(properties);
        return connectionType;
    }

    private void close (ComChannel comChannel) {
        if (comChannel != null) {
            comChannel.close();
        }
    }

}