package com.energyict.protocolimpl.iec1107.emh.lzqj;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.iec1107.FlagIEC1107Connection;
import com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Copyrights EnergyICT
 * Date: 11-mrt-2011
 * Time: 14:33:03
 */
public class DummyFlagConnection extends FlagIEC1107Connection {

    /**
     * the expected responses
     */
    private List<byte[]> responseList = new ArrayList<byte[]>();
    private int counter = 0;
    private byte[] dataReadout;
    private byte[] responseByte;

    public DummyFlagConnection(InputStream inputStream, OutputStream outputStream, int iTimeout, int iMaxRetries, long lForceDelay, int iEchoCancelling, int iIEC1107Compatible, boolean software7E1) throws ConnectionException {
        super(inputStream, outputStream, iTimeout, iMaxRetries, lForceDelay, iEchoCancelling, iIEC1107Compatible, software7E1);
    }

    /**
     * Class constructor.
     *
     * @param inputStream        InputStream for the active connection
     * @param outputStream       OutputStream for the active connection
     * @param iTimeout           Time in ms. for a request to wait for a response before returning an timeout error.
     * @param iMaxRetries        nr of retries before fail in case of a timeout or recoverable failure
     * @param lForceDelay        delay before send. Some protocols have troubles with fast send/receive
     * @param iEchoCancelling    echo cancelling on/off
     * @param iIEC1107Compatible behave full compatible or use protocol special features
     * @param encryptor          if the protocol has an encryptor to use for security level 2 password logon
     * @throws com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException
     *
     */
    public DummyFlagConnection(InputStream inputStream, OutputStream outputStream, int iTimeout, int iMaxRetries, long lForceDelay, int iEchoCancelling, int iIEC1107Compatible, Encryptor encryptor, boolean software7E1) throws ConnectionException {
        super(inputStream, outputStream, iTimeout, iMaxRetries, lForceDelay, iEchoCancelling, iIEC1107Compatible, encryptor, software7E1);
    }

    public DummyFlagConnection(InputStream inputStream, OutputStream outputStream, int iTimeout, int iMaxRetries, long lForceDelay, int iEchoCancelling, int iIEC1107Compatible, Encryptor encryptor, HalfDuplexController halfDuplexController, boolean software7E1) throws ConnectionException {
        super(inputStream, outputStream, iTimeout, iMaxRetries, lForceDelay, iEchoCancelling, iIEC1107Compatible, encryptor, halfDuplexController, software7E1);
    }

    /**
     * Constructor with an additional argument
     *
     * @param inputStream          The used InputStream
     * @param outputStream         The used OutputStream
     * @param iTimeout             The timeout before throwing a ConnectionException
     * @param iMaxRetries          The number of retry attempts
     * @param lForceDelay          The delay before sending a new command
     * @param iEchoCancelling      Indicate whether to ignore the echo from the modem
     * @param iIEC1107Compatible   Indicate whether it is a fully compatible IEC1107 protocol to use specific read commands
     * @param encryptor            The used Encryptor
     * @param halfDuplexController Indication whether to use halfduplex
     * @param software7E1          Indicate whether a GSM modem is used to reformat the data structure
     * @param dontSendBreakCommand Indicate whether it is allowed to send a break command before a retry (some modems see this as a logoff)
     * @throws com.energyict.dialer.connection.ConnectionException
     *          The result of unexpected behavior of the connection
     */
    public DummyFlagConnection(InputStream inputStream, OutputStream outputStream, int iTimeout, int iMaxRetries, long lForceDelay, int iEchoCancelling, int iIEC1107Compatible, Encryptor encryptor, HalfDuplexController halfDuplexController, boolean software7E1, boolean dontSendBreakCommand) throws ConnectionException {
        super(inputStream, outputStream, iTimeout, iMaxRetries, lForceDelay, iEchoCancelling, iIEC1107Compatible, encryptor, halfDuplexController, software7E1, dontSendBreakCommand);
    }

    /**
     * Method that requests a MAC connection for the HDLC layer. this request negotiates some parameters
     * for the buffersizes and windowsizes.
     */
    @Override
    public MeterType connectMAC() throws FlagIEC1107ConnectionException {
        return null;
    }

    @Override
    public MeterType connectMAC(String strIdentConfig, String strPass, int iSecurityLevel, String meterID, int baudrate) throws FlagIEC1107ConnectionException {
        return null;
    }

    @Override
    public MeterType connectMAC(String strIdentConfig, String strPass, int iSecurityLevel, String meterID) throws FlagIEC1107ConnectionException {
        return null;
    }

    @Override
    public byte[] dataReadout(String strIdent, String meterID) throws FlagIEC1107ConnectionException {
        return this.dataReadout;
    }

    @Override
    public void sendRawCommandFrame(byte[] command, byte[] rawdata) throws FlagIEC1107ConnectionException {
        //TODO do nothing
    }

    /**
     * @return the data without the brackets
     * @throws java.io.IOException
     * @throws com.energyict.dialer.connection.ConnectionException
     *
     * @throws com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException
     *
     */
    @Override
    public byte[] receiveData() throws FlagIEC1107ConnectionException {
        return this.responseList.get(counter++);
    }

    /**
     * @return the data including the brackets
     * @throws java.io.IOException
     * @throws com.energyict.dialer.connection.ConnectionException
     *
     * @throws com.energyict.protocolimpl.iec1107.FlagIEC1107ConnectionException
     *
     */
    @Override
    public byte[] receiveRawData() throws FlagIEC1107ConnectionException {
        return this.responseList.get(counter++);
    }

    /**
     * Set the desired response for your next DLMS request.
     *
     * @param response - the response you would like to receive
     */
    public void setResponseByte(byte[] response) {
        if (this.responseList != null) {
            this.responseList.add(response);
        }
    }

    public void setDataReadOut(byte[] dataReadout) {
        this.dataReadout = dataReadout.clone();
    }
}

