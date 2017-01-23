/*
 * IEC1107Connection.java
 *
 * Created on 17 september 2003, 9:31
 */

package com.energyict.dialer.connection;

import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.common.NestedIOException;
import com.energyict.mdc.protocol.api.device.data.MeterDataReadout;
import com.energyict.mdc.protocol.api.dialer.connection.ConnectionException;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOn;
import com.energyict.mdc.protocol.api.inbound.MeterType;
import com.energyict.protocols.mdc.inbound.general.MeterTypeImpl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Koen
 *         Changes:
 *         KV 24012005 Add 1 sec delay after break between datadump and mode C
 */
public class IEC1107HHUConnection extends Connection implements HHUSignOn {

    private static final Logger LOGGER = Logger.getLogger(IEC1107HHUConnection.class.getName());

    private final int TIMEOUT = 600000;

    int[] baudrates = {300, 600, 1200, 2400, 4800, 9600, 19200};

    SerialCommunicationChannel commChannel;
    boolean hhuConnected = false;
    int timeout;
    int maxRetries;

    String receivedIdent = null;
    MeterDataReadout meterDataReadout = null;

    int protocol = 0, mode = 0;
    boolean booleanDataReadout = false;

    byte[] dataDump; // Used with ProtocolTester...

    /**
     * Creates a new instance of IEC1107Connection
     */
    public IEC1107HHUConnection(SerialCommunicationChannel commChannel, int timeout, int maxRetries, long lForceDelay, int iEchoCancelling) throws ConnectionException {
        super(commChannel.getInputStream(), commChannel.getOutputStream(), lForceDelay, iEchoCancelling);
        this.timeout = timeout;
        this.commChannel = commChannel;
        this.maxRetries = maxRetries;
    } // public IEC1107Connection(...)

    /**
     * Method that sends the IEC1107 break sequence.
     *
     * @throws ConnectionException
     */
    public void sendBreak() throws NestedIOException, ConnectionException {
        try {
            byte[] buffer = {SOH, (byte) 0x42, (byte) 0x30, ETX, (byte) 0x71};
            sendRawData(buffer);
            return;
        } catch (ConnectionException e) {
            flushInputStream();
            throw new ConnectionException("sendBreak() error, " + e.getMessage());
        }
    } // public void sendBreak() throws ConnectionException

    /*
    *  Method to receive IEC1107 identificationstring /XXXZidentCRLF
    *  @return received identification
    *  @exception ConnectionException
    */

    private String receiveString(boolean parityCheck) throws NestedIOException, ConnectionException {
        long lMSTimeout;
        int iNewKar;
        String strIdent = "";
        byte[] convert = new byte[1];

        lMSTimeout = System.currentTimeMillis() + timeout;

        copyEchoBuffer();
        String convertstr;

        while (true) {

            if ((iNewKar = readIn()) != -1) {
                if (parityCheck) {
                    iNewKar &= 0x7F;
                } // mask paritybit! if 7,E,1 cause we know we always receive ASCII here!

                if ((byte) iNewKar == NAK) {
                    sendBreak();
                }

                convert[0] = (byte) iNewKar;
                convertstr = new String(convert);

                // if different from CR and LF, add to received string
                if (((byte) iNewKar != LF) && ((byte) iNewKar != CR)) {
                    strIdent += convertstr;
                    if (convertstr.compareTo("\\") == 0) {
                        strIdent += convertstr;
                    }
                }

                if ((byte) iNewKar == LF) {
                    return strIdent;
                }
            } // if ((iNewKar = readIn()) != -1)

            if (((System.currentTimeMillis() - lMSTimeout)) > 0) {
                throw new ConnectionException("receiveIdent() timeout error", TIMEOUT_ERROR);
            }

        } // while(true)

    } // public String receiveString(boolean parityCheck) throws ConnectionException

    private final byte STATE_WAIT_FOR_START = 0;
    //private final byte STATE_WAIT_FOR_LENGTH=1;
    //private final byte STATE_WAIT_FOR_DATA=2;
    private final byte STATE_WAIT_FOR_END = 3;
    private final byte STATE_WAIT_FOR_CHECKSUM = 4;

    private byte[] receiveRawData() throws NestedIOException, ConnectionException {
        return doReceiveData();
    }

    private byte[] doReceiveData() throws NestedIOException, ConnectionException {
        long lMSTimeout, lMSTimeoutInterFrame;
        int iNewKar;
        int iState;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream resultArrayOutputStream = new ByteArrayOutputStream();
        byte calculatedChecksum;
        boolean end;


        // init
        iState = STATE_WAIT_FOR_START;
        end = false;
        lMSTimeout = System.currentTimeMillis() + TIMEOUT;
        lMSTimeoutInterFrame = System.currentTimeMillis() + timeout;
        resultArrayOutputStream.reset();
        byteArrayOutputStream.reset();

        LOGGER.fine("doReceiveData(...):");
        copyEchoBuffer();

        while (true) {

            if ((iNewKar = readIn()) != -1) {
                switch (iState) {
                    case STATE_WAIT_FOR_START: {

                        if ((byte) iNewKar == SOH) {
                            iState = STATE_WAIT_FOR_END;
                        }
                        if ((byte) iNewKar == STX) {
                            iState = STATE_WAIT_FOR_END;
                        }
                        if ((byte) iNewKar == ACK) {
                            return ("ACK".getBytes());
                        }

                    }
                    break; // STATE_WAIT_FOR_START

                    case STATE_WAIT_FOR_END: {
                        lMSTimeoutInterFrame = System.currentTimeMillis() + timeout;
                        if ((byte) iNewKar == ETX) {
                            end = true;
                            iState = STATE_WAIT_FOR_CHECKSUM;
                        } else if ((byte) iNewKar == EOT) {
                            end = false;
                            iState = STATE_WAIT_FOR_CHECKSUM;
                        }
                        byteArrayOutputStream.write(iNewKar);

                    }
                    break; // STATE_WAIT_FOR_END

                    case STATE_WAIT_FOR_CHECKSUM: {
                        byteArrayOutputStream.write(iNewKar);
                        calculatedChecksum = calcChecksum(byteArrayOutputStream.toByteArray());
                        dataDump = byteArrayOutputStream.toByteArray();
                        if (calculatedChecksum == byteArrayOutputStream.toByteArray()[byteArrayOutputStream.toByteArray().length - 1]) {
                            // remove head and tail from byteArrayOutputStream.toByteArray()...
                            byte[] data = new byte[byteArrayOutputStream.toByteArray().length - 2];
                            for (int i = 0; i < (byteArrayOutputStream.toByteArray().length - 2); i++) {
                                data[i] = byteArrayOutputStream.toByteArray()[i];
                            }
                            try {
                                resultArrayOutputStream.write(data);
                            } catch (IOException e) {
                                throw new ConnectionException("receiveStreamData(), IOException, " + e.getMessage());
                            }

                            if (end) {
                                return resultArrayOutputStream.toByteArray();
                            }

                            // init
                            iState = STATE_WAIT_FOR_START;
                            lMSTimeout = System.currentTimeMillis() + TIMEOUT;
                            lMSTimeoutInterFrame = System.currentTimeMillis() + timeout;
                            byteArrayOutputStream.reset();
                            end = false;

                            sendRawData(ACK);
                        } else {
                            throw new ConnectionException("doReceiveData() bad CRC error", CRC_ERROR);
                        }

                    } //break; // STATE_WAIT_FOR_CRC

                } // switch(iState)

            } // if ((iNewKar = readIn()) != -1)

            if (((System.currentTimeMillis() - lMSTimeout)) > 0) {
                throw new ConnectionException("doReceiveData() response timeout error", TIMEOUT_ERROR);
            }
            if (((System.currentTimeMillis() - lMSTimeoutInterFrame)) > 0) {
                throw new ConnectionException("doReceiveData() interframe timeout error", TIMEOUT_ERROR);
            }


        }

    }

    public MeterType signOn(String strIdentConfig, String nodeId) throws IOException {
        return signOn(strIdentConfig, nodeId, 0);
    }

    public MeterType signOn(String strIdentConfig, String nodeId, int baudrate) throws IOException {
        return signOn(strIdentConfig, nodeId, false, baudrate);
    }


    public MeterType signOn(String strIdentConfig, String nodeId, boolean wakeup, int baudrate) throws IOException {
        MeterType meterType;
        if (isDataReadoutEnabled()) {
            doSignOn(strIdentConfig, nodeId, 0, 0, wakeup, baudrate);
            sendBreak();
            delay(1000); // KV 24012005
        }
        meterType = doSignOn(strIdentConfig, nodeId, protocol, mode, wakeup, baudrate);
        return meterType;
    }

    private MeterType doSignOn(String strIdentConfig, String nodeId, int protocol, int mode, boolean wakeup, int baudrate) throws IOException {
        int retries = 0;
        while (true) {
            try {
                // delay 1 second...
                delay(1000);
                if (baudrate != -1) {
                    switchBaudrate(baudrate, 0);
                } // set initial baudrate

                if (wakeup) {
                    wakeUp();
                }

                // build and send flag sequence
                String str = new String("/?" + (nodeId == null ? "" : nodeId) + "!\r\n");
                sendRawData(str.getBytes());
                // receive identification
                receivedIdent = receiveString(false);

//                if (receivedIdent.charAt(0) != '/')
//                    throw new ConnectionException("doSignOn, receivedIdent.charAt(0) != '/', try again!");

                // remove rubbisch in front...
                if (receivedIdent.indexOf('/') == -1) {
                    throw new ConnectionException("signOn() invalid response received '/' missing! (" + receivedIdent + ")");
                }
                receivedIdent = new String(this.getSubArray(receivedIdent.getBytes(), receivedIdent.indexOf('/')));

                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("--->receivedIdent: " + receivedIdent);
                }
                MeterType meterType = new MeterTypeImpl(receivedIdent);
                sendProtocolAckAndSwitchBaudrate(meterType, mode, protocol);

                // receive dataReadout
                if (mode == MODE_READOUT) {
                    LOGGER.fine("--->start data readout...");
                    meterDataReadout = new MeterDataReadout(receiveRawData());
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("--->\n" + String.valueOf(meterDataReadout.getDataReadout()));
                    }
                }

                if ((mode == MODE_BINARY_HDLC) && (protocol == PROTOCOL_HDLC)) {
                    String receivedACK = receiveString(true);
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.fine("--->received confirmation, " + receivedACK);
                    }
                }

                if ((strIdentConfig != null) && ("".compareTo(strIdentConfig) != 0)) {
                    if (strIdentConfig.compareTo(receivedIdent) != 0) {
                        sendBreak();
                        throw new IOException("Wrong identification, " + receivedIdent);
                    }
                }

                if (getMode() == MODE_MANUFACTURER_SPECIFIC_SEVCD) {
                    commChannel.setParams(baudrates[meterType.getBaudrateIndex()], SerialCommunicationChannel.DATABITS_8, SerialCommunicationChannel.PARITY_NONE, SerialCommunicationChannel.STOPBITS_1);
                }

                hhuConnected = true;
                return meterType;
            } catch (ConnectionException e) {
                sendBreak();
                if (retries++ >= maxRetries) {
                    throw new ConnectionException("signOn() error iMaxRetries, " + e.getMessage());
                } else {
                    delay(300);
                }
            }
        }
    }

    protected byte[] getSubArray(byte[] data, int from) {
        byte[] subArray = new byte[data.length - from];
        for (int i = 0; i < subArray.length; i++) {
            subArray[i] = data[i + from];
        }
        return subArray;
    }

    private void sendProtocolAckAndSwitchBaudrate(MeterType meterType, int mode, int protocol) throws IOException {
        // build and send ack sequence
        byte[] ack = {ACK, (byte) (protocol + 0x30), (byte) meterType.getZ(), (byte) (mode + 0x30), (byte) 0x0D, (byte) 0x0A};
//System.out.println("KV_DEBUG> "+new String(ack));
        // seems to be necessary to avoid optical latency effect...
        delay(150); // was 50 KV 16062005 changed to 150 to avoid timing problems when not using baudrate switching. Tested again with A1700, ZMD, SL7000, Iskra and seems OK!
        sendRawDataNoDelay(ack);
        // Delay before changing the baudrate!
        // I tried to use the SerialPortEvent onNotifyOutputEmpty but it doesn't work
        // because the event is not fired too early!
        delay300baudForDatalength(ack);
        // change baudrate to Z baudrate
        switchBaudrate(meterType.getBaudrateIndex(), protocol);
    }

    private void switchBaudrate(int Z, int protocol) throws ConnectionException {
        try {
            if (Z > (baudrates.length - 1)) {
                throw new ConnectionException("HHUConnection, switchBaudrate, protocol sync error, wait 1 min and try again!");
            }
            if (protocol == PROTOCOL_NORMAL) {
                commChannel.setParams(baudrates[Z], SerialCommunicationChannel.DATABITS_7, SerialCommunicationChannel.PARITY_EVEN, SerialCommunicationChannel.STOPBITS_1);
            } else if (protocol == PROTOCOL_HDLC) {
                commChannel.setParams(baudrates[Z], SerialCommunicationChannel.DATABITS_8, SerialCommunicationChannel.PARITY_NONE, SerialCommunicationChannel.STOPBITS_1);
            }
        } catch (IOException e) {
            throw new ConnectionException("HHUConnection, switchBaudrate, ATDialerException, " + e.getMessage());
        }
    }

    /**
     * Method that requests a MAC disconnect for the IEC1107 layer.
     *
     * @throws NestedIOException, ConnectionException
     */
//    private void disconnectHHU() throws NestedIOException,ConnectionException {
//        if (hhuConnected==true) {
//            try {
//                byte[] buffer = {(byte)SOH,(byte)0x42,(byte)0x30,(byte)ETX,(byte)0x71};
//                sendRawData(buffer);
//                hhuConnected=false;
//                return;
//            }
//            catch(ConnectionException e) {
//                flushInputStream();
//                throw new ConnectionException("disconnectMAC() error, "+e.getMessage());
//            }
//
//        } // if (hhuConnected==true)
//
//    } // public void disconnectHHU() throws ConnectionException
    private void delay300baudForDatalength(byte[] ack) throws NestedIOException {
        // calc sleeptime using 300 baud and length of data
        try {
            Thread.sleep((ack.length * 10 * 1000) / 300);
        } catch (InterruptedException e) {
            throw new NestedIOException(e);
        }
    }

    public byte[] getDataReadout() {
        return meterDataReadout.getDataReadout();
    }

    public String getReceivedIdent() {
        return receivedIdent;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public void enableDataReadout(boolean enabled) {
        booleanDataReadout = enabled;
    }

    private boolean isDataReadoutEnabled() {
        return booleanDataReadout;
    }

    public void wakeUp() throws NestedIOException, ConnectionException {
        try {
            long timeout = System.currentTimeMillis() + 2300;
            while (true) {
                sendOut((byte) 0);
                if (((System.currentTimeMillis() - timeout)) > 0) {
                    break;
                }
            }
            Thread.sleep(1700);
            flushInputStream();
        } catch (ConnectionException e) {
            throw new ConnectionException("wakeUp() error " + e.getMessage());
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Caught excetpion", e);
        }
    }

    /**
     * Getter for property protocol.
     *
     * @return Value of property protocol.
     */
    public int getProtocol() {
        return protocol;
    }

    /**
     * Getter for property mode.
     *
     * @return Value of property mode.
     */
    public int getMode() {
        return mode;
    }

}
