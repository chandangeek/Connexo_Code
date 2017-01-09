package com.energyict.dlms;

import com.energyict.protocol.exceptions.ConnectionCommunicationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 5/17/12
 * Time: 9:53 AM
 */
public class IF2LinkLayer {

    /**
     * This is the time to wait if there is no data available until we check again
     */
    private static final int POLL_INTERVAL = 10;

    /**
     * The default buffer size of the remote device (300 bytes), used to add a default delay of {@link IF2LinkLayer#DEFAULT_DEVICE_BUFFER_DELAY}
     */
    public static final int DEFAULT_DEVICE_BUFFER_SIZE = 300;

    /**
     * The default delay to wait between two blocks of data with the size of the {@link IF2LinkLayer#DEFAULT_DEVICE_BUFFER_SIZE}
     */
    public static final int DEFAULT_DEVICE_BUFFER_DELAY = 10;

    private final InputStream inputStream;
    private final OutputStream outputStream;
    private final Logger logger;
    private final int timeout;
    private final int deviceBufferSize;
    private final int deviceBufferDelay;

    /**
     * This debug parameter can enable and disable extensive logging of all RX and TX data
     */
    private boolean debug = false;

    /**
     * The IF2 link layer is responsible for reading and writing raw IF2 packets from
     * and to a given input and output stream. This layer is blocking until a valid packet
     * arrives, or the timeout is exceeded.
     *
     * @param inputStream       The stream to read IF2Packets from
     * @param outputStream      The stream to write IF2Packets to
     * @param timeout           The timeout to use while reading IF2Packets in milli seconds
     * @param deviceBufferSize  The size buffer of the remote device. We will only write blocks of this size at once, and wait for a configured time between these blocks
     *                          The default remote buffer size should be 300 for the AM110R.
     * @param deviceBufferDelay The time to wait in millis between sending blocks of size 'deviceBufferSize'.
     *                          This is implemented like this to give the remote device enough time to catch up with our large apdus.
     * @param logger            The logger to use. If the logger is null, there's a new internal logger created
     */
    public IF2LinkLayer(final InputStream inputStream, final OutputStream outputStream, final int timeout, final int deviceBufferSize, final int deviceBufferDelay, final Logger logger) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.timeout = timeout;
        this.deviceBufferSize = deviceBufferSize;
        this.deviceBufferDelay = deviceBufferDelay;
        this.logger = logger != null ? logger : Logger.getLogger(getClass().getName());
    }

    /**
     * The IF2 link layer is responsible for reading and writing raw IF2 packets from
     * and to a given input and output stream. This layer is blocking until a valid packet
     * arrives, or the timeout is exceeded. This constructor uses the default values for the
     * remote deviceBufferSize and deviceBufferDelay (300 bytes and 50 ms)
     *
     * @param inputStream  The stream to read IF2Packets from
     * @param outputStream The stream to write IF2Packets to
     * @param timeout      The timeout to use while reading IF2Packets in milli seconds
     * @param logger       The logger to use. If the logger is null, there's a new internal logger created
     */
    public IF2LinkLayer(final InputStream inputStream, final OutputStream outputStream, final int timeout, final Logger logger) {
        this(inputStream, outputStream, timeout, DEFAULT_DEVICE_BUFFER_SIZE, DEFAULT_DEVICE_BUFFER_DELAY, logger);
    }

    /**
     * The internal states of the IF2 link layer state machine
     */
    private enum IF2State {
        WAIT_FOR_START,
        WAIT_FOR_ADDRESS,
        WAIT_FOR_LEN_LOW,
        WAIT_FOR_LEN_HIGH,
        WAIT_FOR_HEADER_CRC,
        WAIT_FOR_DATA,
        WAIT_FOR_DATA_CRC_LOW,
        WAIT_FOR_DATA_CRC_HIGH
    }

    /**
     * The logger of this IF2 Link layer. This logger should always be available, and cannot be null
     *
     * @return The logger
     */
    private Logger getLogger() {
        return this.logger;
    }

    /**
     * Enable or disable the debugging mode. (Disabled by default)
     * When the IF2LinkLayer is in debugging mode, it will dump al the received and transmitted IF2 packets to the logger.
     *
     * @param debug True to enable the debugging mode
     */
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    /**
     * Try to read a new IF2 packet from the device
     *
     * @return a new IF2 packet
     * @throws java.io.IOException if there was an error while reading the next IF2 packet
     */
    public final IF2Packet read() throws IOException {

        long readTimeout = System.currentTimeMillis() + this.timeout;

        IF2State state = IF2State.WAIT_FOR_START;
        IF2Packet if2Packet = new IF2Packet();

        do {
            final int value = readNextByte();
            if (value != -1) {

                switch (state) {

                    case WAIT_FOR_START: {
                        if (value == IF2Packet.ST) {
                            state = IF2State.WAIT_FOR_ADDRESS;
                        }
                        break;
                    }

                    case WAIT_FOR_ADDRESS: {
                        if2Packet.setAddress(value);
                        state = IF2State.WAIT_FOR_LEN_LOW;
                        break;
                    }

                    case WAIT_FOR_LEN_LOW: {
                        if2Packet.setLength(value);
                        state = IF2State.WAIT_FOR_LEN_HIGH;
                        break;
                    }

                    case WAIT_FOR_LEN_HIGH: {
                        if2Packet.setLength(if2Packet.getLength() + (value * 256));
                        state = IF2State.WAIT_FOR_HEADER_CRC;
                        break;
                    }

                    case WAIT_FOR_HEADER_CRC: {
                        if2Packet.setHeaderCrc(value);
                        if (if2Packet.isHeaderCrcValid()) {
                            if2Packet.initData();
                            state = IF2State.WAIT_FOR_DATA;
                        } else {
                            getLogger().warning("Received invalid header CRC [" + value + "]! [Packet=" + if2Packet + "]");
                            state = IF2State.WAIT_FOR_START;
                        }
                        break;
                    }

                    case WAIT_FOR_DATA: {
                        if (if2Packet.fillNextDataByte(value)) {
                            state = IF2State.WAIT_FOR_DATA_CRC_LOW;
                        }
                        break;
                    }

                    case WAIT_FOR_DATA_CRC_LOW: {
                        if2Packet.setDataCrc(value);
                        state = IF2State.WAIT_FOR_DATA_CRC_HIGH;
                        break;
                    }

                    case WAIT_FOR_DATA_CRC_HIGH: {
                        if2Packet.setDataCrc(if2Packet.getDataCrc() + (value * 256));
                        if (if2Packet.isDataCrcValid()) {
                            if (debug) {
                                getLogger().info("RX = " + DLMSUtils.getHexStringFromBytes(if2Packet.toByteArray()));
                            }
                            return if2Packet;
                        } else {
                            getLogger().log(Level.WARNING, "Received invalid data CRC [" + value + "]! [Packet=" + if2Packet + "]");
                            state = IF2State.WAIT_FOR_START;
                        }
                        break;
                    }

                    default: {
                        throw new IllegalStateException("Unknown state [" + state + "] in IF2 state machine!");
                    }
                }
            }
        } while (System.currentTimeMillis() < readTimeout);
        throw new IOException("Timeout while reading IF2 packet [timeout=" + this.timeout + " ms]");
    }

    /**
     * Send the given IF2Packet to the connected IF2 interface, taking the remote buffer size and delay in account
     *
     * @param packet The IF2Packet to send to the device on the IF2 interface
     * @throws java.io.IOException              If the occurred an error while sending the packet
     * @throws IllegalArgumentException If the given packet was 'null'
     */
    public final void write(final IF2Packet packet) throws IOException {
        if (packet == null) {
            throw new IllegalArgumentException("Cannot send 'null' packet!");
        }

        final byte[] packetBytes = packet.toByteArray();
        if (debug) {
            getLogger().info(("TX = " + DLMSUtils.getHexStringFromBytes(packetBytes)));
        }
        try {
            if (packetBytes.length <= deviceBufferSize) {
                for (byte packetByte : packetBytes) {
                    this.outputStream.write(packetByte);
                    this.outputStream.flush();
                }
                Thread.sleep(this.deviceBufferDelay);
            } else {
                int totalBytesSend = 0;
                while (totalBytesSend < packetBytes.length) {
                    final int bytesLeftToSend = packetBytes.length - totalBytesSend;
                    final boolean fullBuffer = bytesLeftToSend < this.deviceBufferSize;
                    byte[] smallDeviceBuffer = new byte[fullBuffer ? this.deviceBufferSize : bytesLeftToSend];
                    System.arraycopy(packetBytes, totalBytesSend, smallDeviceBuffer, 0, smallDeviceBuffer.length);
                    Thread.sleep(this.deviceBufferDelay);

                    for (byte bufferByte : smallDeviceBuffer) {
                        this.outputStream.write(bufferByte);
                        this.outputStream.flush();
                    }

                    Thread.sleep(this.deviceBufferDelay);
                    totalBytesSend += smallDeviceBuffer.length;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        }

    }

    /**
     * Try to read the next byte from the device. This is a non-blocking read.
     *
     * @return the next byte or -1 if no bytes are available
     * @throws java.io.IOException Throws an IOException if we were unable to read the next byte
     *                     or if the end of the stream was reached (received '-1')
     */
    private int readNextByte() throws IOException {
        try {
            if (inputStream.available() != 0) {
                final int value = inputStream.read();
                if (value == -1) {
                    throw new IOException("End of stream reached. (Received '-1').");
                }
                return value;
            } else {
                Thread.sleep(POLL_INTERVAL);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        }
        return -1;
    }


}
