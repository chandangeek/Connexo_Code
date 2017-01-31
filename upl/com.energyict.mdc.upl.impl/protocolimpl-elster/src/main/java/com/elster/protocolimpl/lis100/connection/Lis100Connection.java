package com.elster.protocolimpl.lis100.connection;

import com.energyict.mdc.upl.io.NestedIOException;

import com.energyict.dialer.connection.Connection;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Implementation of dsfg protocol. <br>
 * <br>
 * <p/>
 * <b>General Description:</b><br>
 * <br>
 * <br>
 *
 * @author gh
 * @since 5-mai-2010
 */
@SuppressWarnings({"unused"})
public class Lis100Connection extends Connection {

    int iProtocolTimeout = 20000;
    int iProtocolRetry = 3;

    /* status of device lock */
    boolean lockIsOpen = false;

    InputStream input;
    OutputStream output;

    static byte[] hexChar = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};


    /**
     * constructor of lis100 protocol
     *
     * @param inputStream  - incoming serial data
     * @param outputStream - outgoing serial data
     * @throws com.energyict.dialer.connection.ConnectionException
     *          - in case of an exception
     */
    public Lis100Connection(InputStream inputStream, OutputStream outputStream)
            throws ConnectionException {
        super(inputStream, outputStream);
        this.input = inputStream;
        this.output = outputStream;
    }

    public int getProtocolTimeout() {
        return iProtocolTimeout;
    }

    public void setProtocolTimeout(int iProtocolTimeout) {
        this.iProtocolTimeout = iProtocolTimeout;
    }

    public int getProtocolRetry() {
        return iProtocolRetry;
    }

    public void setProtocolRetry(int iProtocolRetry) {
        this.iProtocolRetry = iProtocolRetry;
    }

    /**
     * wait for one char of the end device
     *
     * @return the char read
     * @throws ConnectionException - in case of timeout
     * @throws NestedIOException   - in case of io errors
     */
    private int receiveChar() throws NestedIOException, ConnectionException {

        long lMSTimeout = System.currentTimeMillis() + iProtocolTimeout;

        int iReceivedChar;

        try {
            for (; ; ) {
                if (input.available() != 0) {
                    return input.read();
                }
                Thread.sleep(5L);
                if (System.currentTimeMillis() > lMSTimeout) {
                    break;
                }
            }
        } catch (IOException e) {
            throw new ConnectionException("receiveChar(): IOException " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        }
        throw new ConnectionException("receiveChar() timeout error", TIMEOUT_ERROR);
    }


    /**
     * calculate the checksum of a given data
     *
     * @param data - the data to build the checksum of
     * @return checksum - of the data
     */
    private int calculateChecksum(byte[] data) {
        int result = 0;
        for (byte b : data) {
            if (b < 0) {
                result += ((b & 0x7f) + 0x80);
            } else {
                result += b;
            }
        }
        return result;
    }

    /**
     * send a string to the device
     *
     * @param order - order letter
     * @param data  - data to send (as a string)
     * @throws IOException         - in case of io error
     * @throws ConnectionException - in case of connection problems
     */
    public void sendTelegram(byte order, String data) throws IOException {
        sendTelegram(order, data.getBytes());
    }

    /**
     * send a byte array to the device
     *
     * @param order - order letter
     * @param data  - data to send (as byte array)
     * @throws IOException         - in case of io error
     * @throws ConnectionException - in case of connection problems
     */
    public void sendTelegram(byte order, byte[] data) throws IOException {

        byte bReceivedChar;

        for (int retryCounter = 0; retryCounter < iProtocolRetry; retryCounter++) {
            /* send order */
            sendRawData((byte) '!');
            sendRawData(order);

            try {
                /* wait for acknowledge */
                bReceivedChar = (byte) receiveChar();
                if (bReceivedChar != order) {
                    if (retryCounter >= iProtocolRetry) {
                        throw new ConnectionException(
                                "sendTelegram(): order not acknowledged error", PROTOCOL_ERROR);
                    } else {
                        continue;
                    }
                }

                ByteArrayOutputStream buffer = new ByteArrayOutputStream();

                /* write data */
                buffer.write(data);

                /* calculate checksum over data */
                int checkSum = calculateChecksum(data) & 0xFF;

                /* send '%', checksum and CR LF*/
                buffer.write('%');
                buffer.write(hexChar[checkSum >> 4]);
                buffer.write(hexChar[checkSum & 0xF]);
                buffer.write(CR);
                buffer.write(LF);

                output.write(buffer.toByteArray());

                /* get block acknowledge */
                bReceivedChar = (byte) receiveChar();
                if (bReceivedChar != '+') {
                    if (retryCounter >= iProtocolRetry) {
                        throw new ConnectionException(
                                "sendTelegram(): block not acknowledged error", PROTOCOL_ERROR);
                    } else {
                        continue;
                    }
                }
                break;
            } catch (Exception ignore) {
            }
        }
    }

    /**
     * receive a telegram
     *
     * @param order - order letter
     * @return read data
     * @throws com.energyict.dialer.connection.ConnectionException
     *          if timeout occurs or bcc is wrong
     * @throws NestedIOException in case of io errors
     */
    public String receiveTelegram(byte order) throws ConnectionException,
            NestedIOException {

        String result;
        String msg = "";

        byte bReceivedChar;

        int retryCounter = 0;
        for (; ; ) {
            if (++retryCounter >= iProtocolRetry) {
                throw new ConnectionException(msg, PROTOCOL_ERROR);
            }

            /* send order */
            sendRawData((byte) '?');
            sendRawData(order);

            try {
                /* wait for acknowledge */
                bReceivedChar = (byte) receiveChar();
                if (bReceivedChar != order) {
                    msg = "receiveTelegram(): order not acknowledged error";
                    continue;
                }

                StringBuilder data = new StringBuilder();
                for (; ; ) {
                    bReceivedChar = (byte) receiveChar();
                    if (bReceivedChar == '%') {
                        break;
                    }
                    data.append((char) bReceivedChar);
                }
                result = data.toString();

                // received '%' -> checksum follows
                char cs1 = (char) receiveChar();
                char cs2 = (char) receiveChar();
                int checksum = Integer.parseInt("" + cs1 + cs2, 16);

                // read CR + LR
                receiveChar();
                receiveChar();

                byte ack = '-';
                if (checksum != (calculateChecksum(result.getBytes()) & 0xFF)) {
                    msg = "receiveTelegram(): checksum failure";
                } else {
                    ack = '+';
                }
                output.write((int)ack);

                if ((order == 'x') || (order == 'y')) {
                    byte ackRet = (byte) receiveChar();
                    if ((ack != '+') || (ackRet != '+')) {
                        continue;
                    }
                }
                break;
            } catch (ConnectionException ce) {
                if (ce.getReason() == TIMEOUT_ERROR) {
                    msg = ce.getMessage();
                } else {
                    throw new ConnectionException(
                            ce.getMessage(), ce.getReason());
                }
            } catch (IOException ioe) {
                throw new ConnectionException("receiveTelegram(): IOException " + ioe.getMessage());
            }
        }
        return result;
    }

    /**
     * starting communication by sending STX
     *
     * @throws java.io.IOException - in case of io errors
     */
    public void connect() throws IOException {

        int i;
        for (i = 0; i < iProtocolRetry; i++) {
            try {
                delay(1000);
//                sendRawData(new byte[]{0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30,
//                        0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30, 0x30});
                /* send STX to initiate communication with lis100 device */
                for (int j = 0; j < 3; j++) {
                    sendRawData(STX);
                    delay(100);
                }
                receiveTelegram((byte) 'l');
                return;
            } catch (ConnectionException ce) {
                if (ce.getReason() != TIMEOUT_ERROR) {
                    throw new ConnectionException(ce.getMessage(), ce.getReason());
                }
            }
        }
        throw new ConnectionException("connect(): not connection", MAX_RETRIES_ERROR);
    }

    public void disconnect() throws NestedIOException, ConnectionException {

        /* send ETX to end communication with lis100 device */
        sendRawData(ETX);
        delay(400);
    }

    /**
     * tries to sign on to a lis100 device (has to be done directly after
     * connection).
     *
     * @param password (has to be of a length of 8 char)
     * @throws java.io.IOException - in case of io errors
     */
    public void signon(String password) throws IOException {

        lockIsOpen = false;
        try {
            sendTelegram((byte) 'c', "00000000");
            lockIsOpen = true;
        } catch (Exception ignore) {

        }

        if (!lockIsOpen) {
            try {
                String pwd = (password.trim() + "00000000").substring(0, 8);
                sendTelegram((byte) 'c', pwd);
                lockIsOpen = true;
            } catch (Exception ignore) {

            }
        }
    }

    /**
     * return state of lock when tried to signon
     *
     * @return state of device lock
     */
    @SuppressWarnings({"unused"})
    public boolean isLockOpen() {
        return lockIsOpen;
    }
}
