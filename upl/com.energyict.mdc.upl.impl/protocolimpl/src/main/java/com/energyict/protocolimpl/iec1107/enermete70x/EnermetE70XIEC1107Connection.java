package com.energyict.protocolimpl.iec1107.enermete70x;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.mdc.upl.properties.InvalidPropertyException;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.iec1107.IEC1107Connection;
import com.energyict.protocolimpl.meteridentification.MeterTypeImpl;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author sva
 * @since 13/05/2015 - 10:15
 */
public class EnermetE70XIEC1107Connection extends IEC1107Connection {

    private static final int DELAY_BETWEEN_CONSEQUTIVE_REQUESTS = 500;

    public EnermetE70XIEC1107Connection(InputStream inputStream, OutputStream outputStream, int iTimeout, int iMaxRetries, long lForceDelay, int iEchoCancelling, int iIEC1107Compatible, Encryptor encryptor, String errorSignature, boolean software7E1) throws ConnectionException {
        super(inputStream, outputStream, iTimeout, iMaxRetries, lForceDelay, iEchoCancelling, iIEC1107Compatible, encryptor, errorSignature, software7E1);
    }

    @Override
    public MeterType connectMAC(String strIdentConfig, String strPass, int iSecurityLevel, String meterID) throws IOException {
        if (getIEC1107Compatible() < 2 || getHhuSignOn() != null) {
            // if property 'ProtocolCompatible' is not set to use advanced logon (not set to 2-6)
            // of if hhuSignOn is used (~ and thus using optical head connection, for which the 1.5s timing should not be an issue)
            // then the regular logon should be used
            return super.connectMAC(strIdentConfig, strPass, iSecurityLevel, meterID);
        } else {
           // Do advanced logon
            // Sequence of /? - 051 - P1(x) must be received by the device within max 1,5 seconds from previous request.
            // If the next request is not received within 1,5 sec, then the device will switch to datadump mode and respond with dump
            // We want to avoid this, and send out the 3 commands after each other and parse/validate only afterwards the 3 responses.

            if (!isBoolFlagIEC1107Connected()) {
                setSecurityLevel(iSecurityLevel);
                try {
                    sendOutSignOn(meterID);
                    prepareAndSendOutAuthentication(strPass);
                    MeterType meterType = receiveAndValidateConnectMACResponses(strIdentConfig);
                    setBoolFlagIEC1107Connected(true);
                    return meterType;
                } catch (ProtocolConnectionException e) {
                    throw new ProtocolConnectionException("connectMAC(), ProtocolConnectionException " + e.getMessage(), e.getReason());
                }
            }
            return null;
        }
    }

    private void sendOutSignOn(String meterID) throws IOException {
        String str = "/?" + meterID + "!\r\n";
        sendRawData(str.getBytes());
        delay(DELAY_BETWEEN_CONSEQUTIVE_REQUESTS);
        byte[] ack = {ACK, (byte) 0x30, (byte) (0x30 + (getConfiguredBaudRateCode(getIEC1107Compatible()))), (byte) 0x31, (byte) 0x0D, (byte) 0x0A};
        sendRawData(ack);
    }

    private void prepareAndSendOutAuthentication(String strPass) throws IOException {
        String pwd = (strPass != null) ? strPass : "";
        if (getSecurityLevel() == 0) {
            // No authentication needed
        } else if (getSecurityLevel() == 1 || getSecurityLevel() == 2) {
            setAuthenticationCommand(getSecurityLevel() == 1 ? LOGON_LEVEL_1 : LOGON_LEVEL_2);
            setAuthenticationData(buildData(pwd));

            getEchoByteArrayOutputStream().reset();
            delay(DELAY_BETWEEN_CONSEQUTIVE_REQUESTS);
            sendRawCommandFrame(getAuthenticationCommand(), getAuthenticationData()); // logon using securitylevel
        } else {
            throw new ProtocolConnectionException("FlagIEC1107Connection: invalid SecurityLevel", SECURITYLEVEL_ERROR);
        }
    }

    private MeterType receiveAndValidateConnectMACResponses(String strIdentConfig) throws IOException {
        // 1. Receive identification
        String strIdentRaw = receiveIdent(strIdentConfig);
        if (strIdentRaw.indexOf('/') == -1) {
            throw new ProtocolConnectionException("EnermetE70XIEC1107Connection, connectMAC failed: received invalid identification response, '/' missing! (" + strIdentRaw + ")");
        }
        String strIdent = new String(ProtocolUtils.getSubArray(strIdentRaw.getBytes(), strIdentRaw.indexOf('/')));

        // 2. Receive logon response
        if (getSecurityLevel() == 0) {
            receiveData();  // skip response
        } else if (getSecurityLevel() == 1) {
            skipCommandMessage(); // P0
            String resp = receiveString();
            if (resp.compareTo("B0") == 0) {
                throw new ProtocolConnectionException("EnermetE70XIEC1107Connection, connectMAC failed: authentication failed, received B0 (" + resp + ") instead of ACK");
            } else if (resp.contains("(ER")) {
                throw new ProtocolConnectionException("EnermetE70XIEC1107Connection, connectMAC failed: authentication failed, received ERR (" + resp + ") instead of ACK");
            } else if (resp.compareTo("ACK") != 0) {
                throw new ProtocolConnectionException("EnermetE70XIEC1107Connection, connectMAC failed: authentication failed, received invalid response (" + resp + ") instead of ACK");
            }
        }

        return new MeterTypeImpl(strIdent);
    }

    /**
     * Extract the configured baud rate code from the given iec1107Comptatible integer
     * <ul>
     * <li>2 = 300 baud</li>
     * <li>3 = 1200 baud</li>
     * <li>4 = 2400 baud baud</li>
     * <li>5 = 4800 baud</li>
     * <li>6 = 9600 baud</li>
     * </ul>
     *
     * @param iec1107Compatible
     * @return
     */
    private int getConfiguredBaudRateCode(int iec1107Compatible) throws InvalidPropertyException {
        if (iec1107Compatible < 1 || iec1107Compatible > 6) {
            throw new InvalidPropertyException("Property ProtocolCompatible contains invalid value (" + iec1107Compatible + "), only values 1 to 6 are allowed");
        }

        return iec1107Compatible - 1;
    }
}