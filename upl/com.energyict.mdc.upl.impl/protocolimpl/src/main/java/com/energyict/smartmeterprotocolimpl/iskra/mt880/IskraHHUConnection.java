package com.energyict.smartmeterprotocolimpl.iskra.mt880;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.IEC1107HHUConnection;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.mdc.io.NestedIOException;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.meteridentification.MeterTypeImpl;
import com.energyict.protocolimpl.utils.ProtocolUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;

/**
 * IEC1107 HHU connection for the Iskra MT880.
 * The connection is similar to the regular {@link IEC1107HHUConnection}.
 * It starts on baud 300 (7E1), after which it can switch to a higher baud (up to 38400).
 * The communication settings should remain 7E1 through whole communication.
 *
 * <p/>
 * Copyrights EnergyICT
 * Date: 2/05/13
 * Time: 11:00
 * Author: khe
 */
public class IskraHHUConnection extends IEC1107HHUConnection {

    private static final Log logger = LogFactory.getLog(IskraHHUConnection.class);
    private SerialCommunicationChannel commChannel;
    private int[] baudrates = {300, 600, 1200, 2400, 4800, 9600, 19200, 38400};
    private int timeout;
    private int maxRetries;
    private String receivedIdent = null;

    /**
     * Creates a new instance of IEC1107Connection
     */
    public IskraHHUConnection(SerialCommunicationChannel commChannel, int timeout, int maxRetries, long lForceDelay, int iEchoCancelling) {
        super(commChannel, timeout, maxRetries, lForceDelay, iEchoCancelling);
        this.commChannel = commChannel;
        this.timeout = timeout;
        this.maxRetries = maxRetries;
    }

    public MeterType signOn(String strIdentConfig, String nodeId, boolean wakeup, int baudrate) throws IOException {
        return doSignOn(strIdentConfig, nodeId, getProtocol(), getMode(), wakeup, baudrate);
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
                if (logger.isDebugEnabled()) {
                    ProtocolUtils.outputHex((iNewKar));
                }

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
            }
            if (System.currentTimeMillis() - lMSTimeout > 0) {
                throw new ConnectionException("receiveIdent() timeout error", TIMEOUT_ERROR);
            }
        }
    }

    private void delay300baudForDatalength(byte[] ack) {
        // calc sleeptime using 300 baud and length of data
        try {
            Thread.sleep((ack.length * 10 * 1000) / 300);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw ConnectionCommunicationException.communicationInterruptedException(e);
        }
    }

    private void sendProtocolAckAndSwitchBaudrate(MeterType meterType, int mode, int protocol) throws IOException {
        // build and send ack sequence
        byte[] ack = {ACK, (byte) (protocol + 0x30), (byte) meterType.getZ(), (byte) (mode + 0x30), (byte) 0x0D, (byte) 0x0A};
        // seems to be necessary to avoid optical latency effect...
        delay(150); // was 50 KV 16062005 changed to 150 to avoid timing problems when not using baudrate switching. Tested again with A1700, ZMD, SL7000, Iskra and seems OK!
        sendRawDataNoDelay(ack);
        // Delay before changing the baudrate!
        // I tried to use the SerialPortEvent onNotifyOutputEmpty but it doesn't work
        // because the event is not fired too early!
        delay300baudForDatalength(ack);
        // change baudrate to Z baudrate
        switchBaudrate(meterType.getBaudrateIndex(), protocol);
        delay(2000);
    }


    private MeterType doSignOn(String strIdentConfig, String nodeId, int protocol, int mode, boolean wakeup, int baudrate) throws IOException {
        int retries = 0;
        while (true) {
            try {
                // delay 1 second...
                delay(1000);
                if (baudrate != -1) {
                    switchBaudrate(baudrate, PROTOCOL_NORMAL);  // Switch baudrate, line control should be 7E1!
                } // set initial baudrate

                if (wakeup) {
                    wakeUp();
                }

                // build and send flag sequence
                String str = "/?" + (nodeId == null ? "" : nodeId) + "!\r\n";
                sendRawData(str.getBytes());
                // receive identification
                receivedIdent = receiveString(false);

                // remove rubbisch in front...
                if (receivedIdent.indexOf('/') == -1) {
                    throw new ConnectionException("signOn() invalid response received '/' missing! (" + receivedIdent + ")");
                }
                receivedIdent = new String(ProtocolUtils.getSubArray(receivedIdent.getBytes(), receivedIdent.indexOf('/')));

                if (logger.isDebugEnabled()) {
                    logger.debug("--->receivedIdent: " + receivedIdent);
                }
                MeterType meterType = new MeterTypeImpl(receivedIdent);
                sendProtocolAckAndSwitchBaudrate(meterType, mode, protocol);

//                if (mode == MODE_BINARY_HDLC) {
//                    String receivedACK = receiveString(true);
//                    if (logger.isDebugEnabled()) {
//                        logger.debug("--->received confirmation, " + receivedACK);
//                    }
//                }

                if ((strIdentConfig != null) && ("".compareTo(strIdentConfig) != 0)) {
                    if (strIdentConfig.compareTo(receivedIdent) != 0) {
                        sendBreak();
                        throw new IOException("Wrong identification, " + receivedIdent);
                    }
                }

                if (getMode() == MODE_MANUFACTURER_SPECIFIC_SEVCD) {
                    commChannel.setParams(baudrates[meterType.getBaudrateIndex()], SerialCommunicationChannel.DATABITS_8, SerialCommunicationChannel.PARITY_NONE, SerialCommunicationChannel.STOPBITS_1);
                }

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
}
