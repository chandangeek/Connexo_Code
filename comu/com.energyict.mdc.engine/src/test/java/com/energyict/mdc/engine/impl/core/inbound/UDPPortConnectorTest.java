package com.energyict.mdc.engine.impl.core.inbound;

import com.energyict.mdc.engine.FakeServiceProvider;
import com.energyict.mdc.engine.impl.core.ServiceProvider;
import com.energyict.mdc.engine.impl.events.EventPublisherImpl;
import com.energyict.mdc.engine.model.UDPBasedInboundComPort;
import com.energyict.mdc.protocol.api.ComChannel;
import com.energyict.mdc.protocol.api.exceptions.InboundCommunicationException;

import com.elster.jupiter.util.time.Clock;
import com.elster.jupiter.util.time.impl.DefaultClock;
import com.energyict.protocols.mdc.services.SocketService;
import com.energyict.protocols.mdc.services.impl.SocketServiceImpl;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.junit.*;
import org.junit.runner.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Tests for the {@link UDPPortConnector} component
 * <p/>
 * Copyrights EnergyICT
 * Date: 5/11/12
 * Time: 16:31
 */
@RunWith(MockitoJUnitRunner.class)
public class UDPPortConnectorTest {

    public static final int PORT_NUMBER = 4059;
    public static final int NUMBER_OF_SIMULTANEOUS_CONNECTIONS = 2;
    public static final int BUFFER_SIZE = 1024;

    @Mock
    private DatagramSocket datagramSocket;
    @Mock
    private EventPublisherImpl eventPublisher;

    private SocketService socketService;
    private FakeServiceProvider serviceProvider = new FakeServiceProvider();
    private Clock clock = new DefaultClock();

    @Before
    public void mockSocketService () {
        this.socketService = mock(SocketService.class);
    }

    private void useRealSocketService() {
        this.socketService = new SocketServiceImpl();
    }

    @Before
    public void setupServiceProvider () {
        this.serviceProvider.setClock(this.clock);
        ServiceProvider.instance.set(this.serviceProvider);
    }

    @After
    public void resetServiceProvider () {
        ServiceProvider.instance.set(null);
    }

    @Before
    public void setupEventPublisher () {
        EventPublisherImpl.setInstance(this.eventPublisher);
    }

    @After
    public void resetEventPublisher () {
        EventPublisherImpl.setInstance(null);
    }

    @Before
    public void initializeMocksAndFactories () throws IOException {
        when(this.socketService.newUDPSocket(anyInt())).thenReturn(this.datagramSocket);
        ArgumentCaptor<DatagramPacket> datagramPacketArgumentCaptor = ArgumentCaptor.forClass(DatagramPacket.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                DatagramPacket datagramPacket = (DatagramPacket) invocationOnMock.getArguments()[0];
                datagramPacket.setPort(PORT_NUMBER);
                return null;
            }
        }).when(this.datagramSocket).receive(datagramPacketArgumentCaptor.capture());
    }

    private UDPBasedInboundComPort createUDPBasedInboundComPort() {
        UDPBasedInboundComPort udpBasedInboundComPort = mock(UDPBasedInboundComPort.class);
        when(udpBasedInboundComPort.getPortNumber()).thenReturn(PORT_NUMBER);
        when(udpBasedInboundComPort.getNumberOfSimultaneousConnections()).thenReturn(NUMBER_OF_SIMULTANEOUS_CONNECTIONS);
        when(udpBasedInboundComPort.getBufferSize()).thenReturn(BUFFER_SIZE);
        return udpBasedInboundComPort;
    }

    @Test(timeout = 2000)
    public void testProperAccept() throws IOException {
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable {
                if (invocationOnMock.getArguments()[0] instanceof DatagramPacket) {
                    DatagramPacket packet = (DatagramPacket) invocationOnMock.getArguments()[0];
                    packet.setPort(PORT_NUMBER);
                }
                return null;  // really nothing to do, but we need to come back instead of block
            }
        }).when(datagramSocket).receive(any(DatagramPacket.class));

        UDPBasedInboundComPort udpBasedInboundComPort = createUDPBasedInboundComPort();

        UDPPortConnector udpPortConnector = new UDPPortConnector(udpBasedInboundComPort, socketService);

        // Business method
        ComChannel accept = udpPortConnector.accept();

        // asserts
        assertThat(accept).isNotNull();
        assertThat(accept).isInstanceOf(ComPortRelatedComChannel.class);
    }

    @Test(timeout = 1000, expected = InboundCommunicationException.class)
    public void testConstructorFailure() throws InboundCommunicationException, SocketException {
        doThrow(new SocketException("Something fishy happened for testing purposes")).when(this.socketService).newUDPSocket(anyInt());

        UDPBasedInboundComPort udpBasedInboundComPort = createUDPBasedInboundComPort();

        try {
            // business method
            new UDPPortConnector(udpBasedInboundComPort, socketService);
        } catch (InboundCommunicationException e) {
            if (!"CSC-COM-403".equalsIgnoreCase(e.getMessageId())) {
                fail("Message should have indicated that their was an exception during the setup of the inbound call, but was " + e.getMessage());
            } else {
                throw e;
            }
        }
    }

    @Test(timeout = 2000, expected = InboundCommunicationException.class)
    public void testAcceptFailure() throws IOException, InboundCommunicationException {
        doThrow(new IOException("Something fishy happened during the accept for testing purposes")).
            when(this.datagramSocket).
            receive(any(DatagramPacket.class));

        UDPBasedInboundComPort udpBasedInboundComPort = createUDPBasedInboundComPort();

        UDPPortConnector udpPortConnector = new UDPPortConnector(udpBasedInboundComPort, socketService);

        try {
            // Business method
            udpPortConnector.accept();
        } catch (InboundCommunicationException e) {
            if (!"CSC-COM-403".equalsIgnoreCase(e.getMessageId())) {
                fail("Message should have indicated that their was an exception during the setup of the inbound call, but was " + e.getMessage());
            } else {
                throw e;
            }
        }
    }

    @Test(timeout = 5000)
    public void readWithByteArrayTest() throws InterruptedException {
        this.useRealSocketService();
        CountDownLatch answerCounter = new CountDownLatch(2);
        final String firstAnswer = "Properly received your connect";
        final String secondAnswer = "Bye!";

        List<String> receivedResponses = new ArrayList<>();   // received from the client

        UDPBasedInboundComPort udpBasedInboundComPort = createUDPBasedInboundComPort();
        UDPPortConnector connector = new UDPPortConnector(udpBasedInboundComPort, socketService);

        final UdpClient udpClient = new UdpClient(answerCounter);
        Thread udpClientThread = new Thread(udpClient);
        udpClientThread.setName("UdpClientThread for readWithByteTest");
        udpClientThread.start();

        ComChannel comChannel = connector.accept();

        byte[] read = new byte[BUFFER_SIZE];
        int numberOfBytesRead = comChannel.read(read);
        receivedResponses.add(new String(Arrays.copyOfRange(read, 0, numberOfBytesRead)));

        comChannel.startWriting();
        comChannel.write(firstAnswer.getBytes());

        read = new byte[BUFFER_SIZE];
        comChannel.startReading();
        numberOfBytesRead = comChannel.read(read);
        receivedResponses.add(new String(Arrays.copyOfRange(read, 0, numberOfBytesRead)));

        comChannel.startWriting();
        comChannel.write(secondAnswer.getBytes());

        answerCounter.await();

        assertThat(receivedResponses).hasSize(2);
        assertThat(receivedResponses).containsOnly(UdpClient.FIRST_RESPONSE, UdpClient.SECOND_RESPONSE);

        assertThat(udpClient.getAnswers()).hasSize(2);
        assertThat(udpClient.getAnswers()).containsOnly(firstAnswer, secondAnswer);

        comChannel.close();
        udpClientThread.interrupt();
    }

    @Test(timeout = 5000)
    public void readWithByteArrayAndLengthAndOffsetTest() throws InterruptedException {
        this.useRealSocketService();
        CountDownLatch answerCounter = new CountDownLatch(2);

        final String firstAnswer = "Properly received your connect";
        final String secondAnswer = "Bye!";

        List<String> receivedResponses = new ArrayList<>();   // received from the client

        UDPBasedInboundComPort udpBasedInboundComPort = createUDPBasedInboundComPort();
        UDPPortConnector connector = new UDPPortConnector(udpBasedInboundComPort, socketService);

        final UdpClient udpClient = new UdpClient(answerCounter);
        Thread udpClientThread = new Thread(udpClient);
        udpClientThread.setName("UdpClientThread for readWithLengthAndOffset");
        udpClientThread.start();

        ComChannel comChannel = connector.accept();

        byte[] read = new byte[BUFFER_SIZE];
        int numberOfBytesRead = comChannel.read(read, 0, read.length);
        receivedResponses.add(new String(Arrays.copyOfRange(read, 0, numberOfBytesRead)));

        comChannel.startWriting();
        comChannel.write(firstAnswer.getBytes());

        read = new byte[BUFFER_SIZE];
        comChannel.startReading();
        numberOfBytesRead = comChannel.read(read, 0, read.length);
        receivedResponses.add(new String(Arrays.copyOfRange(read, 0, numberOfBytesRead)));

        comChannel.startWriting();
        comChannel.write(secondAnswer.getBytes());

        answerCounter.await();

        assertThat(receivedResponses).hasSize(2);
        assertThat(receivedResponses).containsOnly(UdpClient.FIRST_RESPONSE, UdpClient.SECOND_RESPONSE);

        assertThat(udpClient.getAnswers()).hasSize(2);
        assertThat(udpClient.getAnswers()).containsOnly(firstAnswer, secondAnswer);

        comChannel.close();
        udpClientThread.interrupt();
    }

    @Test(timeout = 5000)
    public void readWithByteByByteTest() throws InterruptedException {
        this.useRealSocketService();
        CountDownLatch answerCounter = new CountDownLatch(2);

        final String firstAnswer = "Properly received your connect";
        final String secondAnswer = "Bye!";

        List<String> receivedResponses = new ArrayList<>();   // received from the client

        UDPBasedInboundComPort udpBasedInboundComPort = createUDPBasedInboundComPort();
        UDPPortConnector connector = new UDPPortConnector(udpBasedInboundComPort, socketService);

        UdpClient udpClient = new UdpClient(answerCounter);
        Thread udpClientThread = new Thread(udpClient);
        udpClientThread.setName("UdpClientThread for readByteByByteTest");
        udpClientThread.start();

        ComChannel comChannel = connector.accept();

        ByteBuffer byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        comChannel.startReading();
        byteBuffer.put((byte) comChannel.read());   // do this so the initial read is added

        while (comChannel.available() > 0) {
            byteBuffer.put((byte) comChannel.read());
        }

        receivedResponses.add(new String(Arrays.copyOfRange(byteBuffer.array(), 0, byteBuffer.position())));

        comChannel.startWriting();
        comChannel.write(firstAnswer.getBytes());

        byteBuffer = ByteBuffer.allocate(BUFFER_SIZE);
        comChannel.startReading();
        byteBuffer.put((byte) comChannel.read());   // do this so the initial read is added

        while (comChannel.available() > 0) {
            byteBuffer.put((byte) comChannel.read());
        }

        receivedResponses.add(new String(Arrays.copyOfRange(byteBuffer.array(), 0, byteBuffer.position())));

        comChannel.startWriting();
        comChannel.write(secondAnswer.getBytes());

        answerCounter.await();

        assertThat(receivedResponses).hasSize(2);
        assertThat(receivedResponses).containsOnly(UdpClient.FIRST_RESPONSE, UdpClient.SECOND_RESPONSE);

        assertThat(udpClient.getAnswers()).hasSize(2);
        assertThat(udpClient.getAnswers()).containsOnly(firstAnswer, secondAnswer);

        comChannel.close();
        udpClientThread.interrupt();
    }

    @Test(timeout = 50000)
    public void testWriteByteByByte() throws InterruptedException, IOException {
        this.useRealSocketService();
        CountDownLatch answerCounter = new CountDownLatch(2);

        final String firstAnswer = "Properly received your connect";
        final String secondAnswer = "Bye!";

        List<String> receivedResponses = new ArrayList<>();   // received from the client

        UDPBasedInboundComPort udpBasedInboundComPort = createUDPBasedInboundComPort();
        UDPPortConnector connector = new UDPPortConnector(udpBasedInboundComPort, socketService);

        final UdpClient udpClient = new UdpClient(answerCounter);
        Thread udpClientThread = new Thread(udpClient);
        udpClientThread.setName("UdpClientThread for writeByteByByteTest");
        udpClientThread.start();

        ComChannel comChannel = connector.accept();

        byte[] read = new byte[BUFFER_SIZE];
        int numberOfBytesRead = comChannel.read(read);
        receivedResponses.add(new String(Arrays.copyOfRange(read, 0, numberOfBytesRead)));

        comChannel.startWriting();
        for (byte b : firstAnswer.getBytes()) {
            comChannel.write(b);
        }
        comChannel.flush();

        read = new byte[BUFFER_SIZE];
        comChannel.startReading();
        numberOfBytesRead = comChannel.read(read);
        receivedResponses.add(new String(Arrays.copyOfRange(read, 0, numberOfBytesRead)));

        comChannel.startWriting();
        for (byte b : secondAnswer.getBytes()) {
            comChannel.write(b);
        }
        comChannel.flush();

        answerCounter.await();

        assertThat(receivedResponses).hasSize(2);
        assertThat(receivedResponses).containsOnly(UdpClient.FIRST_RESPONSE, UdpClient.SECOND_RESPONSE);

        assertThat(udpClient.getAnswers()).hasSize(2);
        assertThat(udpClient.getAnswers()).containsOnly(firstAnswer, secondAnswer);

        comChannel.close();
        udpClientThread.interrupt();
    }

    /**
     * Small UDP client that will send some data to a listening server on the localhost
     */
    private class UdpClient implements Runnable {

        private static final String FIRST_RESPONSE = "This is for a test!";
        private static final String SECOND_RESPONSE = "GoodBye";
        private final List<String> sendAnswers;
        private CountDownLatch answerCounter;

        private UdpClient(CountDownLatch answerCounter) {
            this.answerCounter = answerCounter;
            this.sendAnswers = new ArrayList<>();
        }

        @Override
        public void run() {
            try {
                String dataToSend = FIRST_RESPONSE;
                byte[] receiveData = new byte[BUFFER_SIZE];
                DatagramSocket ds = new DatagramSocket();
                ds.connect(InetAddress.getByName("localhost"), PORT_NUMBER);
                ds.send(new DatagramPacket(dataToSend.getBytes(), dataToSend.length()));

                DatagramPacket datagramPacket = new DatagramPacket(receiveData, receiveData.length);
                ds.receive(datagramPacket);
                addAnswer(new String(Arrays.copyOfRange(receiveData, 0, datagramPacket.getLength())));
                answerCounter.countDown();
                // saying goodbye
                ds.send(new DatagramPacket(SECOND_RESPONSE.getBytes(), SECOND_RESPONSE.length(), datagramPacket.getSocketAddress()));

                // receive bye
                receiveData = new byte[BUFFER_SIZE];
                datagramPacket = new DatagramPacket(receiveData, receiveData.length);
                ds.receive(datagramPacket);
                addAnswer(new String(Arrays.copyOfRange(receiveData, 0, datagramPacket.getLength())));
                answerCounter.countDown();

                // add additional receive so the thread won't close
                ds.receive(datagramPacket);
            } catch (IOException e) {
                e.printStackTrace();
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
