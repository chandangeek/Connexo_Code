package com.energyict.protocolimplv2.hhusignon;

import com.energyict.dialer.connection.Connection;
import com.energyict.dlms.protocolimplv2.CommunicationSessionProperties;
import com.energyict.mdc.channels.serial.BaudrateValue;
import com.energyict.mdc.channels.serial.NrOfDataBits;
import com.energyict.mdc.channels.serial.NrOfStopBits;
import com.energyict.mdc.channels.serial.Parities;
import com.energyict.mdc.channels.serial.SerialPortConfiguration;
import com.energyict.mdc.io.CommunicationException;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.mdc.protocol.api.dialer.core.HHUSignOnV2;
import com.energyict.mdc.protocol.api.inbound.MeterType;
import com.energyict.protocol.exceptions.ConnectionException;
import com.energyict.protocols.mdc.inbound.general.MeterTypeImpl;
import com.energyict.protocols.mdc.services.impl.MessageSeeds;
import com.energyict.protocols.util.ProtocolUtils;

import java.io.IOException;
import java.math.BigDecimal;

/**
 * Implements the IEC1107 HHU sign on. This does the baud rate switching procedure.
 * Use this for all protocolimplV2 protocols.
 * <p/>
 * Only the sign on is currently implemented, feel free to complete this class :)
 * <p/>
 * Copyrights EnergyICT
 * Date: 8/11/13
 * Time: 12:27
 * Author: khe
 */
public class IEC1107HHUSignOn implements HHUSignOnV2 {

    private final SerialPortComChannel comChannel;
    private int protocol = 0;  //Defines the line control parameters. 0: 7E1, 2: 8N1
    private int mode = 0;
    private long timeout;
    private int retries;
    private String receivedIdentificationString = null;

    private final int[] baudrates = {300, 600, 1200, 2400, 4800, 9600, 19200};

    public IEC1107HHUSignOn(SerialPortComChannel comChannel, CommunicationSessionProperties properties) {
        this.comChannel = comChannel;
        this.timeout = properties.getTimeout();
        this.retries = properties.getRetries();
    }

    @Override
    public MeterType signOn(String strIdent, String meterID) {
        return signOn(strIdent, meterID, 0);
    }

    @Override
    public MeterType signOn(String strIdent, String meterID, int baudrate) {
        return signOn(strIdent, meterID, false, baudrate);
    }

    @Override
    public MeterType signOn(String strIdent, String meterID, boolean wakeup, int baudrate) {
        return doSignOn(strIdent, meterID, protocol, mode, false, baudrate);
    }

    /**
     * The IEC1107 sign on procedure:
     * 1. Set the initial baud rate and line control
     * 2. (optional wake up)
     * 3. Send ID request frame:    /?id!
     * 4. Receive meter identification and proposed serial parameters
     * 5. Ack and set serial parameters
     * 6. Receive meter acknowledgement
     */
    private MeterType doSignOn(String strIdentConfig, String nodeId, int protocol, int mode, boolean wakeup, int baudrate) {
        int attempt = 0;
        while (true) {
            try {
                delay(1000);
                if (baudrate != -1) {
                    switchBaudrate(baudrate, 0);    // set initial baudrate
                }
                if (wakeup) {
                    wakeUp();
                }

                // build and send flag sequence
                String idRequest = "/?" + (nodeId == null ? "" : nodeId) + "!\r\n";
                sendOut(idRequest.getBytes());
                // receive identification
                receivedIdentificationString = receiveString(false);

                // remove rubbisch in front...
                if (receivedIdentificationString.indexOf('/') == -1) {
                    throw new CommunicationException(MessageSeeds.PROTOCOL_CONNECT_FAILED, "signOn() invalid response received '/' missing! (" + receivedIdentificationString + ")");
                }
                receivedIdentificationString = new String(ProtocolUtils.getSubArray(receivedIdentificationString.getBytes(), receivedIdentificationString.indexOf('/')));

                MeterType meterType;
                try {
                    meterType = new MeterTypeImpl(receivedIdentificationString);
                } catch (IOException e) {
                    throw new CommunicationException(MessageSeeds.PROTOCOL_CONNECT_FAILED, e);
                }
                sendProtocolAckAndSwitchBaudrate(meterType, mode, protocol);

                if ((mode == MODE_BINARY_HDLC) && (protocol == PROTOCOL_HDLC)) {
                    String receivedACK = receiveString(true);
                }

                if ((strIdentConfig != null) && ("".compareTo(strIdentConfig) != 0)) {
                    if (strIdentConfig.compareTo(receivedIdentificationString) != 0) {
                        sendBreak();
                        IOException ioException = new IOException("Wrong identification, " + receivedIdentificationString);
                        throw new CommunicationException(MessageSeeds.PROTOCOL_CONNECT_FAILED, ioException);
                    }
                }

                return meterType;
            } catch (IOException e) {
                if (attempt++ >= this.retries) {
                    if (e instanceof ConnectionException) {     //Something went wrong, unexpected response
                        throw new CommunicationException(MessageSeeds.PROTOCOL_CONNECT_FAILED, e);
                    }                                           //Actual timeout
                    throw new CommunicationException(MessageSeeds.NUMBER_OF_RETRIES_REACHED, this.retries + 1);
                } else {
                    sendBreak();
                    delay(300);
                }
            }
        }
    }

    /**
     * Send an acknowledgement, and switch the baud rate and line control.
     * After this, the HDLC session can start.
     */
    private void sendProtocolAckAndSwitchBaudrate(MeterType meterType, int mode, int protocol) {
        byte[] ack = {Connection.ACK, (byte) (protocol + 0x30), (byte) meterType.getZ(), (byte) (mode + 0x30), (byte) 0x0D, (byte) 0x0A};
        delay(150);
        comChannel.startWriting();
        comChannel.write(ack);
        delay((ack.length * 10 * 1000) / 300);
        switchBaudrate(meterType.getBaudrateIndex(), protocol);
    }

    /**
     * Switch the baud rate and change the line control to 7E1 or 8N1.
     */
    private void switchBaudrate(int baudrateIndex, int protocol) {
        int baudRate = baudrates[baudrateIndex];
        SerialPortConfiguration configuration = comChannel.getSerialPort().getSerialPortConfiguration();
        if (protocol == PROTOCOL_NORMAL) {     //Apply baud rate & 7E1
            configuration.setBaudrate(BaudrateValue.valueFor(BigDecimal.valueOf(baudRate)));
            configuration.setNrOfDataBits(NrOfDataBits.SEVEN);
            configuration.setParity(Parities.EVEN);
            configuration.setNrOfStopBits(NrOfStopBits.ONE);
        } else if (protocol == PROTOCOL_HDLC) {      //Apply baud rate & 8N1
            configuration.setBaudrate(BaudrateValue.valueFor(BigDecimal.valueOf(baudRate)));
            configuration.setNrOfDataBits(NrOfDataBits.EIGHT);
            configuration.setParity(Parities.NONE);
            configuration.setNrOfStopBits(NrOfStopBits.ONE);
        }
        comChannel.getSerialPort().updatePortConfiguration(configuration);
    }

    /**
     * Execute a wake up
     */
    public void wakeUp() {
        long timeout = System.currentTimeMillis() + 2300;
        while (true) {
            sendOut((byte) 0);
            if (((System.currentTimeMillis() - timeout)) > 0) {
                break;
            }
        }
        delay(1700);
        flushInputStream();
    }

    /**
     * Flush the input stream
     */
    protected void flushInputStream() {
        comChannel.startReading();
        while (comChannel.available() != 0) {
            comChannel.read();
        }
    }

    /**
     * Receive a response from the meter.
     *
     * @throws ConnectionException if no response is received after the timeout interval
     */
    private String receiveString(boolean parityCheck) throws IOException {
        int newChar;
        String fullIdentificationString = "";
        long timeoutMoment = System.currentTimeMillis() + timeout;

        String receivedChar;
        comChannel.startReading();

        while (true) {

            if ((newChar = readIn()) != -1) {
                if (parityCheck) {
                    newChar &= 0x7F;
                } // mask paritybit! if 7,E,1 cause we know we always receive ASCII here!

                if ((byte) newChar == Connection.NAK) {
                    sendBreak();
                }

                receivedChar = new String(new byte[]{(byte) newChar});

                // if different from CR and LF, add to received string
                if (((byte) newChar != Connection.LF) && ((byte) newChar != Connection.CR)) {
                    fullIdentificationString += receivedChar;
                    if (receivedChar.compareTo("\\") == 0) {
                        fullIdentificationString += receivedChar;
                    }
                }

                if ((byte) newChar == Connection.LF) {
                    return fullIdentificationString;
                }
            } else {
                delay(100);
            }

            if (System.currentTimeMillis() - timeoutMoment > 0) {
                throw new IOException("receiveIdent() timeout error");
            }
        }
    }

    private int readIn() {
        if (comChannel.available() != 0) {
            return comChannel.read();
        } else {
            return -1;
        }
    }

    /**
     * This defines the line control parameters. 0: 7E1, 2: 8N1
     */
    @Override
    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    /**
     * Set the mode. 2: HDLC.
     */
    @Override
    public void setMode(int mode) {
        this.mode = mode;
    }

    @Override
    public void enableDataReadout(boolean enabled) {
        //Not supported yet
    }

    @Override
    public byte[] getDataReadout() {
        //Not supported yet
        return new byte[0];
    }

    @Override
    public String getReceivedIdent() {
        return receivedIdentificationString;
    }

    /**
     * Method that sends the IEC1107 break sequence.
     */
    @Override
    public void sendBreak() {
        byte[] breakRequest = {Connection.SOH, (byte) 0x42, (byte) 0x30, Connection.ETX, (byte) 0x71};
        sendOut(breakRequest);
    }

    private void delay(long lDelay) {
        try {
            Thread.sleep(lDelay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void sendOut(byte request) {
        sendOut(new byte[]{request});
    }

    private void sendOut(byte[] request) {
        delay(300);       //Forced delay
        comChannel.startWriting();
        comChannel.write(request);
    }
}
