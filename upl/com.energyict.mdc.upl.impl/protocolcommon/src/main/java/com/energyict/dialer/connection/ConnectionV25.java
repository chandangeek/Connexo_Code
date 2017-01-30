/*
 * Connection.java
 *
 * Created on 1 juli 2003, 17:03
 */

package com.energyict.dialer.connection;

import com.energyict.mdc.upl.io.NestedIOException;

import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.protocol.HalfDuplexEnabler;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import serialio.xmodemapi.XGet;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Koen
 */
public abstract class ConnectionV25 implements HalfDuplexEnabler {

    private static final Log logger = LogFactory.getLog(ConnectionV25.class);

    protected final byte UNKNOWN_ERROR = -1;
    protected final byte TIMEOUT_ERROR = -2;
    protected final byte CRC_ERROR = -3;
    protected final byte FRAMING_ERROR = -4;
    protected final byte MAX_RETRIES_ERROR = -5;
    protected final byte FRAME_ERROR = -6;
    protected final byte PROTOCOL_ERROR = -7;
    protected final byte NAK_RECEIVED = -8;


    // First 32 control characters of the ASCII SET
    String[] controlCharacters = {"NUL", "SOH", "STX", "ETX", "EOT", "ENQ", "ACK", "BEL", "BS", "HT", "LF", "VT", "FF", "CR", "SO", "SI",
            "DLE", "DC1", "DC2", "DC3", "DC4", "NAK", "SYN", "ETB", "CAN", "EM", "SUB", "ESC", "FS", "GS", "RS", "US"};
    public static final byte NUL = 0x00;
    public static final byte SOH = 0x01;
    public static final byte STX = 0x02;
    public static final byte ETX = 0x03;
    public static final byte EOT = 0x04;
    public static final byte ENQ = 0x05;
    public static final byte ACK = 0x06;
    public static final byte BEL = 0x07;
    public static final byte BS = 0x08;
    public static final byte HT = 0x09;
    public static final byte LF = 0x0A;
    public static final byte VT = 0x0B;
    public static final byte FF = 0x0C;
    public static final byte CR = 0x0D;
    public static final byte SO = 0x0E;
    public static final byte SI = 0x0F;
    public static final byte DLE = 0x10;
    public static final byte DC1 = 0x11;
    public static final byte DC2 = 0x12;
    public static final byte DC3 = 0x13;
    public static final byte DC4 = 0x14;
    public static final byte NAK = 0x15;
    public static final byte SYN = 0x16;
    public static final byte ETB = 0x17;
    public static final byte CAN = 0x18;
    public static final byte EM = 0x19;
    public static final byte SUB = 0x1A;
    public static final byte ESC = 0x1B;
    public static final byte FS = 0x1C;
    public static final byte GS = 0x1D;
    public static final byte RS = 0x1E;
    public static final byte US = 0x1F;


    private OutputStream outputStream;
    private InputStream inputStream;
    long forcedDelay;
    int iEchoCancelling;

    ByteArrayOutputStream echoByteArrayOutputStream = new ByteArrayOutputStream();
    ByteArrayInputStream echoByteArrayInputStream;
    HalfDuplexController halfDuplexController = null;

    /**
     * Creates a new instance of Connection.
     *
     * @param inputStream          communication inputStream
     * @param outputStream         communication outputStream
     * @param forcedDelay          delay in ms before sendRawData
     * @param iEchoCancelling      on(1)/off(0)
     * @param halfDuplexController HalfDuplexController
     * @throws com.energyict.dialer.connection.ConnectionException
     *          Thrown for communication related exceptions
     */
    protected ConnectionV25(InputStream inputStream,
                            OutputStream outputStream,
                            long forcedDelay,
                            int iEchoCancelling,
                            HalfDuplexController halfDuplexController) {

        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.forcedDelay = forcedDelay;
        this.iEchoCancelling = iEchoCancelling;
        this.halfDuplexController = halfDuplexController;
    }

    /**
     * Creates a new instance of Connection.
     *
     * @param inputStream     communication inputStream
     * @param outputStream    communication outputStream
     * @param forcedDelay     delay in ms before sendRawData
     * @param iEchoCancelling on(1)/off(0)
     * @throws com.energyict.dialer.connection.ConnectionException
     *          Thrown for communication related exceptions
     */
    protected ConnectionV25(InputStream inputStream,
                            OutputStream outputStream,
                            long forcedDelay,
                            int iEchoCancelling) throws ConnectionException {
        this(inputStream, outputStream, forcedDelay, iEchoCancelling, null);
    }

    /**
     * Creates a new instance of Connection without echocancelling and without delay before send.
     *
     * @param inputStream  communication inputStream
     * @param outputStream communication outputStream
     * @throws com.energyict.dialer.connection.ConnectionException
     *          Thrown for communication related exceptions
     */
    protected ConnectionV25(InputStream inputStream,
                            OutputStream outputStream) throws ConnectionException {
        this(inputStream, outputStream, 0, 0);
    }

    /**
     * Send data and wait for the echo of the data returned (waitforEcho=true) or delay each character (waitForEcho=false)
     *
     * @param txbuffer    data to send
     * @param waitForEcho wait for echo enabled or disabled
     * @throws NestedIOException Thrown for all other then communication related exceptions
     * @throws com.energyict.dialer.connection.ConnectionException
     *          Thrown for communication related exceptions
     */
    public void sendOutTerminalMode(byte[] txbuffer, boolean waitForEcho) throws NestedIOException, ConnectionException {

        if (waitForEcho) {
            for (int i = 0; i < txbuffer.length; i++) {
                doSendOut(txbuffer[i]);
                waitForEcho(txbuffer[i]);
            }
        } else {
            for (int i = 0; i < txbuffer.length; i++) {
                delay(forcedDelay);
                doSendOut(txbuffer[i]);
            }
        }
    }

    private void waitForEcho(int echo) throws NestedIOException, ConnectionException {
        long echoTimeout = System.currentTimeMillis() + 5000;
        int kar = 0;
        while (true) {
            if ((kar = readIn()) != -1) {
                if (kar != echo) {
                    throw new ConnectionException("Connection, waitForEcho(), wrong character echo received! (" + (char) echo + "!=" + (char) kar + ")");
                } else {
                    break;
                }
            }

            if (System.currentTimeMillis() - echoTimeout > 0) {
                throw new ConnectionException("Connection, waitForEcho(), timeout waiting for character echo!", TIMEOUT_ERROR);
            }
        } // while(true)
    }

    /**
     * Wait until nothing receives anymore. Use delay as timeout
     *
     * @param delay timeout waiting for empty buffer
     * @throws NestedIOException Thrown for all other then communication related exceptions
     * @throws com.energyict.dialer.connection.ConnectionException
     *          Thrown for communication related exceptions
     */
    public void waitForEmptyBuffer(long delay) throws NestedIOException, ConnectionException {
        long emptyBufferTimeout = System.currentTimeMillis() + delay;
        while (true) {
            if (readIn() != -1) {
                emptyBufferTimeout = System.currentTimeMillis() + delay;
            }
            if (System.currentTimeMillis() - emptyBufferTimeout > 0) {
                break;
            }
        } // while(true)
    }

    /**
     * Perform XMODEM communication and retuen datablock
     *
     * @return data byte[] datablock
     * @throws java.io.IOException throws when something goes wrong
     */
    public byte[] getXmodemProtocolData() throws IOException {
        XGet xget = new XGet(outputStream, inputStream);
//System.out.println("KV_DEBUG> changed xmodem settings... XMODEM should timeout after 10 seconds???? followingg the doc???") ;
        return xget.getBinaryData(10); //3);
        //return xget.get('b', true,true,false, 10, 10); //(10 sec timeout and 10 retries)
        //return xget.get('b',true,true,false,0, 10);
    }

    /**
     * Send 1 byte. This implements also the echocancelling and halfduplex mechanism.
     *
     * @param txbyte byte to send
     * @throws com.energyict.dialer.connection.ConnectionException
     *          Thrown for communication related exceptions
     */
    public void sendOut(byte txbyte) throws ConnectionException {
        byte[] txbuffer = new byte[1];
        txbuffer[0] = txbyte;
        doSendOut(txbuffer, 0, 1);
    }

    /**
     * Send byte array. This implements also the echocancelling and halfduplex mechanism.
     *
     * @param txbuffer byte[] to send
     * @throws com.energyict.dialer.connection.ConnectionException
     *          Thrown for communication related exceptions
     */
    public void sendOut(byte[] txbuffer) throws ConnectionException {
        doSendOut(txbuffer, 0, txbuffer.length);
    }

    /**
     * Send buffer from offset for length. This implements also the echocancelling and halfduplex mechanism.
     *
     * @param txbuffer byte[] to send
     * @param offset   int
     * @param length   int
     * @throws com.energyict.dialer.connection.ConnectionException
     *          Thrown for communication related exceptions
     */
    protected void sendOut(byte[] txbuffer, int offset, int length) throws ConnectionException {
        doSendOut(txbuffer, offset, length);
    }

    private void doSendOut(byte txbuffer) throws ConnectionException {
        try {
            if (iEchoCancelling != 0) {
                echoByteArrayOutputStream.write(txbuffer);
            }
            if (getHalfDuplexController() != null) {
                getHalfDuplexController().request2SendV25(1);
                delay(forcedDelay);
            }
            outputStream.write(txbuffer);
            if (getHalfDuplexController() != null) {
                getHalfDuplexController().request2ReceiveV25(1);
            }
        } catch (IOException e) {
            throw new ConnectionException("Connection, doSendOut() error " + e.getMessage());
        }
    } // private void doSendOut(byte txbuffer)

    private void doSendOut(byte[] txbuffer, int offset, int length) throws ConnectionException {
        try {
            if (iEchoCancelling != 0) {
                echoByteArrayOutputStream.write(txbuffer);
            }
            if (getHalfDuplexController() != null) {
                getHalfDuplexController().request2SendV25(length);
                delay(forcedDelay);
            }
            outputStream.write(txbuffer, offset, length);
            if (getHalfDuplexController() != null) {
                getHalfDuplexController().request2ReceiveV25(length);
            }
        } catch (IOException e) {
            throw new ConnectionException("Connection, doSendOut() error " + e.getMessage());
        }
    } // private void doSendOut(byte[] txbuffer)

    /**
     * Read character from inputstream. sleep 100 ms if nothing available and return -1
     * this is a non blocking method since we need to do timeout management in the sub class calling this method
     *
     * @return character or -1 if none is available
     * @throws NestedIOException Thrown for all other then communication related exceptions
     * @throws com.energyict.dialer.connection.ConnectionException
     *          Thrown for communication related exceptions
     */
    protected int readIn() throws NestedIOException, ConnectionException {
        try {
            int iNewKar;

            if (inputStream.available() != 0) {
                iNewKar = inputStream.read();
                if (iNewKar != echoByteArrayInputStream.read()) {
                    return iNewKar;
                }
            } // if (inputStream.available() != 0)
            else {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ConnectionException("Connection, readIn() error " + e.getMessage());
        }
        return (-1);
    } // private int readIn() throws ConnectionException

    /**
     * Read character array from inputstream. sleep 100 ms if nothing available and return -1
     * this is a non blocking method since we need to do timeout management in the sub class calling this method
     *
     * @return byte[] or null if none is available
     * @throws NestedIOException Thrown for all other then communication related exceptions
     * @throws com.energyict.dialer.connection.ConnectionException
     *          Thrown for communication related exceptions
     */
    protected byte[] readInArray() throws NestedIOException, ConnectionException {
        try {
            byte[] data;
            int len;
            if ((len = inputStream.available()) != 0) {
                data = new byte[len];
                inputStream.read(data, 0, len);
                for (int i = 0; i < len; i++) {
                    if (data[i] != (byte) echoByteArrayInputStream.read()) {
                        return DialerSupport.getSubArray(data, i);
                    }
                }
            } // if (inputStream.available() != 0)
            else {
                Thread.sleep(100);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ConnectionException("Connection, readIn() error " + e.getMessage());
        }
        return (null);
    } // private int readInArray() throws ConnectionException

    /**
     * delay
     *
     * @param lDelay long delay in ms
     * @throws NestedIOException Thrown when something goes wrong
     */
    protected void delay(long lDelay) throws NestedIOException {
        try {
            Thread.sleep(lDelay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        }
    }

    /**
     * flush the echo output buffer. This must be done before a frame send.
     */
    protected void flushEchoBuffer() {
        echoByteArrayOutputStream.reset();
    }

    /**
     * Copy echou output buffer to input. This is used to compare with the received characters
     * on the input stream. Invoke this method before receiving a frame after a frame send.
     */
    protected void copyEchoBuffer() {
        echoByteArrayInputStream = new ByteArrayInputStream(echoByteArrayOutputStream.toByteArray());
    }

    /**
     * delay for ms and then flush inputbuffer
     *
     * @param delay long delay in ms
     * @throws NestedIOException Thrown for all other then communication related exceptions
     * @throws com.energyict.dialer.connection.ConnectionException
     *          Thrown for communication related exceptions
     */
    protected void delayAndFlush(long delay) throws ConnectionException, NestedIOException {
        delay(delay);
        flushInputStream();
    }

    /**
     * Flush all waiting characters ikn the inputstream.
     *
     * @throws com.energyict.dialer.connection.ConnectionException
     *          Thrown for communication related exceptions
     */
    protected void flushInputStream() throws ConnectionException {
        try {
            while (inputStream.available() != 0) {
                inputStream.read();
            } // flush inputbuffer
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            throw new ConnectionException("Connection, flushInputStream() error " + e.getMessage());
        }
    } // private void flushInputStream()  throws ConnectionException

    /**
     * Same as sendOut(byte[] txbuffer) but preceeded by a flushEchoBuffer()
     *
     * @param txbuffer byte[] to send
     * @throws com.energyict.dialer.connection.ConnectionException
     *          Thrown for communication related exceptions
     */
    protected void sendRawDataNoDelay(byte[] txbuffer) throws ConnectionException {
        flushEchoBuffer();
        sendOut(txbuffer);
    } // public void sendRawData(byte[] byteBuffer)

    /**
     * Same as sendOutTerminalMode but  preceeded by a flushEchoBuffer()
     *
     * @param txbuffer    data to send
     * @param waitForEcho wait for echo enabled or disabled
     * @throws NestedIOException Thrown for all other then communication related exceptions
     * @throws com.energyict.dialer.connection.ConnectionException
     *          Thrown for communication related exceptions
     */
    protected void sendRawDataNoDelayTerminalMode(byte[] txbuffer, boolean waitForEcho) throws NestedIOException, ConnectionException {
        flushEchoBuffer();
        sendOutTerminalMode(txbuffer, waitForEcho);
    } // public void sendRawDataNoDelayTerminalMode(byte[] byteBuffer)

    /**
     * Method to send an array of bytes via outputstream. It flushes the echo output buffer before send.
     *
     * @param txbuffer Byte array to send.
     */
    protected void sendRawData(byte[] txbuffer) throws NestedIOException, ConnectionException {
        delay(forcedDelay);
        flushEchoBuffer();
        sendOut(txbuffer);
    } // public void sendRawData(byte[] byteBuffer)

    /**
     * Same as sendOut(byte txbuffer) but preceeded by a delay(lForceDelay) and flushEchoBuffer()
     *
     * @param txbuffer character to send
     * @throws NestedIOException Thrown for all other then communication related exceptions
     * @throws com.energyict.dialer.connection.ConnectionException
     *          Thrown for communication related exceptions
     */
    protected void sendRawData(byte txbuffer) throws NestedIOException, ConnectionException {
        delay(forcedDelay);
        flushEchoBuffer();
        sendOut(txbuffer);
    } // public void sendRawData(byte[] byteBuffer)

    /**
     * Calculate modulo 256 checksum.
     *
     * @param data   byte array to calculate checksum on
     * @param length nr of bytes of data to calculate checksum
     * @param offset offset in byte array to calculate checksum
     * @return byte checksum
     * @throws com.energyict.dialer.connection.ConnectionException
     *          Thrown for communication related exceptions
     */
    protected byte calcChecksum(byte[] data, int length, int offset) throws ConnectionException {
        return (doCalcChecksum(data, length, offset));
    }

    /**
     * Calculate modulo 256 checksum.
     *
     * @param data   byte array to calculate checksum on
     * @param length nr of bytes of data to calculate checksum
     * @return byte checksum
     * @throws com.energyict.dialer.connection.ConnectionException
     *          Thrown for communication related exceptions
     */
    protected byte calcChecksum(byte[] data, int length) throws ConnectionException {
        return (doCalcChecksum(data, length, 0));
    }

    /**
     * Calculate modulo 256 checksum.
     *
     * @param data byte array to calculate checksum on
     * @return byte checksum
     * @throws com.energyict.dialer.connection.ConnectionException
     *          Thrown for communication related exceptions
     */
    protected byte calcChecksum(byte[] data) throws ConnectionException {
        return (doCalcChecksum(data, data.length, 0));
    }

    private byte doCalcChecksum(byte[] data, int length, int offset) throws ConnectionException {
        int checksum = 0;
        if (length > (data.length - offset)) {
            throw new ConnectionException("Connection, doCalcChecksum, datalength=" + data.length + ", length=" + length + ", offset=" + offset);
        }
        for (int i = 0; i < length - 1; i++) {
            checksum ^= (data[offset + i] & 0xff);
        }
        return (byte) checksum;
    }

    /**
     * Getter for property halfDuplexController.
     *
     * @return Value of property halfDuplexController.
     */
    public com.energyict.dialer.core.HalfDuplexController getHalfDuplexController() {
        return halfDuplexController;
    }

    /**
     * Setter for property halfDuplexController.
     *
     * @param halfDuplexController New value of property halfDuplexController.
     */
    public void setHalfDuplexController(com.energyict.dialer.core.HalfDuplexController halfDuplexController) {
        this.halfDuplexController = halfDuplexController;
    }

}