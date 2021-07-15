/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.mdc.channels.ip.datagrams;

import com.energyict.mdc.channels.ip.OutboundIpConnectionType;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.io.SerialComponentService;
import com.energyict.mdc.upl.properties.PropertySpecService;
import com.energyict.mdc.upl.properties.PropertyValidationException;
import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.mdc.upl.TypedProperties;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class OutboundUdpConnectionTypeTest {

    private PropertySpecService propertySpecService;
    private SerialComponentService serialComponentService;

    @Before
    public void initializeMocksAndFactories() {
        propertySpecService = mock(PropertySpecService.class);
        serialComponentService = mock(SerialComponentService.class);
    }

    public static final int PORT_NUMBER = 8080;
    public static final int BUFFER_SIZE = 1024;

    @Test(expected = SocketException.class, timeout = 8000)
    public void udpSessionToUnknownHostTest() throws ConnectionException, SocketException, PropertyValidationException {
        OutboundUdpConnectionType udpConnectionType = new OutboundUdpConnectionType(propertySpecService);

        TypedProperties typedProperties = TypedProperties.empty();
        typedProperties.setProperty(OutboundUdpConnectionType.HOST_PROPERTY_NAME, "www.ditiszekereendomeinnaamdienietbestaat.zelfsdeextentiebestaatniet");
        typedProperties.setProperty(OutboundUdpConnectionType.PORT_PROPERTY_NAME, new BigDecimal(PORT_NUMBER));
        typedProperties.setProperty(OutboundUdpConnectionType.BUFFER_SIZE_NAME, new BigDecimal(BUFFER_SIZE));
        udpConnectionType.setUPLProperties(typedProperties);

        try {
            ComChannel comChannel = udpConnectionType.connect();
            comChannel.write("Hello".getBytes());
        } catch (ConnectionException e) {
            // Expected a ConnectionException but want to assert that the cause is a SocketTimeoutException
            Throwable cause = e.getCause();
            if (cause instanceof SocketException) {
                throw (SocketException) cause;
            }
        }
    }

    @Test(timeout = 50000)
    public void udpConnectToUdpServerTest() throws ConnectionException, InterruptedException, PropertyValidationException {
        CountDownLatch socketCloseLatch = new CountDownLatch(1);
        OutboundUdpConnectionType connectionType = this.newUDPConnectionType();

        final SimpleUdpConnectServer udpServer = new SimpleUdpConnectServer(socketCloseLatch);
        Thread udpServerThread = new Thread(udpServer);
        udpServerThread.setName("UdpServer for connectionTest");
        udpServerThread.start();

        ComChannel comChannel = null;
        try {
            // Business method
            comChannel = connectionType.connect();

            socketCloseLatch.await();

            // Asserts
            assertThat(comChannel).isNotNull();
        } finally {
            this.close(comChannel);
        }
    }

    @Test
    @Ignore
    public void udpReadWriteTest() throws ConnectionException, InterruptedException, PropertyValidationException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch socketCloseLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(1);
        final String firstRequest = "Hello Server, how are you doing?";
        final String secondRequest = "Bye!";
        List<String> receivedResponses = new ArrayList<>();   // received from the client

        OutboundUdpConnectionType connectionType = this.newUDPConnectionType();

        final UdpServer udpServer = new UdpServer(socketCloseLatch, finishLatch, startLatch);
        Thread udpServerThread = new Thread(udpServer);
        udpServerThread.setName("UdpServer for readWriteTest");
        udpServerThread.start();

        ComChannel comChannel = null;
        try {
            startLatch.await();
            // Business method
            comChannel = connectionType.connect();
            comChannel.write(firstRequest.getBytes());

            comChannel.startReading();
            byte[] receiveData = new byte[BUFFER_SIZE];
            int numberOfReadBytes = comChannel.read(receiveData);
            receivedResponses.add(new String(Arrays.copyOfRange(receiveData, 0, numberOfReadBytes)));

            comChannel.startWriting();
            comChannel.write(secondRequest.getBytes());

            comChannel.startReading();
            receiveData = new byte[BUFFER_SIZE];
            numberOfReadBytes = comChannel.read(receiveData);
            receivedResponses.add(new String(Arrays.copyOfRange(receiveData, 0, numberOfReadBytes)));

            socketCloseLatch.await();
            // Asserts
            assertThat(udpServer.getAnswers()).hasSize(2);
            assertThat(udpServer.getAnswers()).containsOnly(firstRequest, secondRequest);

            finishLatch.countDown();
            assertThat(receivedResponses).hasSize(2);
            assertThat(receivedResponses).containsOnly(UdpServer.FIRST_RESPONSE, UdpServer.SECOND_RESPONSE);

        } finally {
            this.close(comChannel);
            udpServerThread.interrupt();
        }
    }

    private OutboundUdpConnectionType newUDPConnectionType() throws PropertyValidationException {
        OutboundUdpConnectionType connectionType = new OutboundUdpConnectionType(propertySpecService);
        TypedProperties properties = TypedProperties.empty();
        properties.setProperty(OutboundIpConnectionType.HOST_PROPERTY_NAME, "localhost");
        properties.setProperty(OutboundIpConnectionType.PORT_PROPERTY_NAME, new BigDecimal(PORT_NUMBER));
        properties.setProperty(OutboundUdpConnectionType.BUFFER_SIZE_NAME, new BigDecimal(BUFFER_SIZE));
        connectionType.setUPLProperties(properties);
        return connectionType;
    }

    private void close(ComChannel comChannel) {
        if (comChannel != null) {
            comChannel.close();
        }
    }

    private class SimpleUdpConnectServer implements Runnable {

        private CountDownLatch socketCloseLatch;

        private SimpleUdpConnectServer(CountDownLatch socketCloseLatch) {
            this.socketCloseLatch = socketCloseLatch;
        }

        @Override
        public void run() {
            DatagramSocket ds = null;
            try {
                ds = new DatagramSocket(PORT_NUMBER);
                while (!ds.isBound()) {  // tests if we have a receiver on the other end
                    Thread.sleep(50);
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            } finally {
                if (ds != null && !ds.isClosed()) {
                    ds.close();
                }
                socketCloseLatch.countDown();
            }
        }
    }

    /**
     * Small UDP server that will allow connecting and reading/writing
     */
    private class UdpServer implements Runnable {

        private static final String FIRST_RESPONSE = "Hello there little test, don't you dare fail on me!";
        private static final String SECOND_RESPONSE = "GoodBye";
        private final List<String> sendAnswers;
        private CountDownLatch socketCloseLatch;
        private CountDownLatch finishLatch;
        private CountDownLatch startLatch;

        public UdpServer(CountDownLatch socketCloseLatch, CountDownLatch finishLatch, CountDownLatch startLatch) {
            this.sendAnswers = new ArrayList<>();
            this.socketCloseLatch = socketCloseLatch;
            this.finishLatch = finishLatch;
            this.startLatch = startLatch;
        }

        @Override
        public void run() {
            DatagramSocket ds = null;
            try {
                ds = new DatagramSocket(PORT_NUMBER);

                byte[] receiveData = new byte[BUFFER_SIZE];
                DatagramPacket datagramPacket = new DatagramPacket(receiveData, receiveData.length);
                startLatch.countDown(); // indicate to the client that he can start sending
                // receive initial greeting
                ds.receive(datagramPacket);
                addAnswer(new String(Arrays.copyOfRange(receiveData, 0, datagramPacket.getLength())));
                // saying hello
                ds.send(new DatagramPacket(FIRST_RESPONSE.getBytes(), FIRST_RESPONSE.length(), datagramPacket.getSocketAddress()));

                receiveData = new byte[BUFFER_SIZE];
                datagramPacket = new DatagramPacket(receiveData, receiveData.length);
                // receive goodbye
                ds.receive(datagramPacket);
                addAnswer(new String(Arrays.copyOfRange(receiveData, 0, datagramPacket.getLength())));
                // say goodbye
                ds.send(new DatagramPacket(SECOND_RESPONSE.getBytes(), SECOND_RESPONSE.length(), datagramPacket.getSocketAddress()));

            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (ds != null && !ds.isClosed()) {
                    ds.close();
                }
                socketCloseLatch.countDown();
                try {
                    finishLatch.await();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        private void addAnswer(String answer) {
            this.sendAnswers.add(answer);
        }

        private List<String> getAnswers() {
            return this.sendAnswers;
        }
    }

}
