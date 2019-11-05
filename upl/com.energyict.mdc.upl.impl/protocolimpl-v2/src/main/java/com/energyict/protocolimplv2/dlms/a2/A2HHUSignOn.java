package com.energyict.protocolimplv2.dlms.a2;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dlms.protocolimplv2.CommunicationSessionProperties;
import com.energyict.mdc.protocol.SerialPortComChannel;
import com.energyict.protocol.exceptions.CommunicationException;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimplv2.hhusignon.IEC1107HHUSignOn;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;

/**
 * Implements the EN62056 21 HHU sign on. creatyed for the ThemisUno gas-meter.
 * <p/>
 * Only the sign on is currently implemented, feel free to complete this class :)
 * <p/>
 * Copyrights EnergyICT
 * Date: 12/02/19
 * Author: h236365
 */
public class A2HHUSignOn extends IEC1107HHUSignOn {
    private static final byte PDU_DELIMITER = (byte) 0x7E;
    private Long timeout = getTimeout();
    private int frameLength;
    private int byteCount;

    private final byte[] snrm = ProtocolTools.getBytesFromHexString("818014050203e8060203e8070400000001080400000001", 2);

    private A2RequestFrameBuilder frameBuilder;

    public A2HHUSignOn(SerialPortComChannel comChannel, CommunicationSessionProperties properties) {
        super(comChannel, properties);
        this.frameBuilder = new A2RequestFrameBuilder(properties);
    }

    public void setClientMacAddress(int clientMacAddress) {
        frameBuilder.setClientMacAddress(clientMacAddress);
    }

    @Override
    protected MeterType doSignOn(String strIdentConfig, String nodeId, int protocol, int mode, boolean wakeup, int baudrate) {
        int attempt = 0;
        while (true) {
            try {
                delay(1000);
                if (baudrate != -1) {
                    switchBaudrate(baudrate, protocol);    // set initial baudrate and other params
                }
                if (wakeup) {
                    wakeUp();
                }
                sendOut(getSNMR());
                receiveString(false);
                return new MeterType(MeterType.A2_THEMISUNO);
            } catch (IOException e) {
                attempt = processException(attempt, e);
            }
        }
    }

    private int processException(int attempt, IOException e) {
        if (attempt++ >= getRetries()) {
            if (e instanceof ConnectionException) {     //Something went wrong, unexpected response
                throw CommunicationException.protocolConnectFailed(e);
            }                                           //Actual timeout
            throw ConnectionCommunicationException.numberOfRetriesReached(e, getRetries() + 1);
        } else {
            sendBreak();
            delay(300);
        }
        return attempt;
    }

    protected byte[] getSNMR() {
        byte[] bytes = frameBuilder.buildSNMRFrame(snrm);
        byte[] frameFlags = frameBuilder.addHDLCFrameFlags(bytes);
        return frameFlags;
    }

    /**
     * Receive a response from the meter, and return it as hexString.
     *
     * @throws ConnectionException if no response is received after the timeout interval
     */

    private String receiveString(boolean parityCheck) throws IOException {
        frameLength = 0;
        byteCount = 0;
        int newChar;
        String fullIdentificationString = "";
        long timeoutMoment = System.currentTimeMillis() + timeout;
        getComChannel().startReading();
        while (true) {
            newChar = readIn();
            if (newChar != -1) {
                byteCount++;
                if (parityCheck) {
                    newChar &= 0x7F;
                } // mask paritybit! if 7,E,1 cause we know we always receive ASCII here!
                String hexString = Hex.toHexString(new byte[]{(byte) newChar});
                fullIdentificationString += hexString;
                calculateFrameLength(fullIdentificationString);
                if (newChar == PDU_DELIMITER && checkLastByte()) {
                    return fullIdentificationString;
                }
            } else {
                if (checkLastByte()) {
                    return fullIdentificationString;
                }
                delay(100);
            }

            if (System.currentTimeMillis() - timeoutMoment > 0) {
                throw new IOException("receiveIdent() timeout error");
            }
        }
    }

    private void calculateFrameLength(String identificationString) {
        if (identificationString.length() == 6) {
            String hex = identificationString.substring(3);
            frameLength = Integer.parseInt(hex, 16) + 2; // length of frame plus 2 bytes padding (7e)
        }
    }

    private boolean checkLastByte() {
        if (frameLength > 0 && byteCount >= frameLength) {
            return true;
        }
        return false;
    }

    public void setFrameBuilder(A2RequestFrameBuilder frameBuilder) {
        this.frameBuilder = frameBuilder;
    }
}

