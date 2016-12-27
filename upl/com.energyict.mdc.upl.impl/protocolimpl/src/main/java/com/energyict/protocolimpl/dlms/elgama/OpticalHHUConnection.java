package com.energyict.protocolimpl.dlms.elgama;

import com.energyict.mdc.io.NestedIOException;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.core.SerialCommunicationChannel;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.meteridentification.MeterTypeImpl;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Copyrights EnergyICT
 * Date: 21-dec-2010
 * Time: 12:04:28
 */
public class OpticalHHUConnection implements HHUSignOn {

    public static final int[] BAUDRATES = {300, 600, 1200, 2400, 4800, 9600, 19200};
    public static final int TIMEOUT = 5000;

    private static final byte[] SIGN_ON = new byte[]{0x2F, 0x3F, 0x21, 0x0D, 0x0A};
    private static final byte[] BREAK = new byte[]{0x01, 0x42, 0x30, 0x03, 0x71};

    private final SerialCommunicationChannel communicationChannel;

    private boolean dataReadOut = false;
    private int mode = MODE_BINARY_HDLC;
    private int protocol = PROTOCOL_HDLC;
    private int baudRate = 0;
    private String receivedIdent = null;

    public OpticalHHUConnection(SerialCommunicationChannel communicationChannel) {
        this.communicationChannel = communicationChannel;
    }

    public void sendBreak() throws NestedIOException, ConnectionException {
        try {
            writeRawData(BREAK);
        } catch (IOException e) {
            throw new ConnectionException("sendBreak() error, " + e.getMessage());
        }
    }

    public MeterType signOn(String strIdent, String meterID) throws IOException, ConnectionException {
        return signOn(strIdent, meterID, 0);
    }

    public MeterType signOn(String strIdent, String meterID, int baudrate) throws IOException, ConnectionException {
        return signOn(strIdent, meterID, false, baudrate);
    }

    public MeterType signOn(String strIdent, String meterID, boolean wakeup, int baudrate) throws IOException, ConnectionException {
        set7E1(baudrate);
        writeRawData(SIGN_ON);
        ProtocolTools.delay(100);
        readIdent();
        writeRawData(new byte[]{0x06, 0x32, 0x35, 0x32, 0x0D, 0x0A});
        ProtocolTools.delay(100);
        writeRawData(new byte[20]);
        ProtocolTools.delay(100);
        set7E1(5);
        readLine();
        return new MeterTypeImpl(getReceivedIdent());
    }

    private void readIdent() throws IOException {
        receivedIdent = readLine();
    }

    private String readLine() throws IOException {
        StringBuilder sb = new StringBuilder();
        long endTime = System.currentTimeMillis() + TIMEOUT;
        while (true) {
            if (getInputStream().available() > 0) {
                int byteIn = getInputStream().read();
                if (byteIn != -1) {
                    byteIn &= 0x07F;
                    if (byteIn == 0x0A) {
                        break;
                    } else if (byteIn != 0x0D) {
                        sb.append((char) byteIn);
                    }
                }
            } else {
                if (System.currentTimeMillis() > endTime) {
                    throw new IOException("Timeout");
                }
                ProtocolTools.delay(10);
            }
        }
        return sb.toString();
    }

    private void set7E1(int baudrate) throws IOException {
        getCommunicationChannel().setParams(
                BAUDRATES[baudrate],
                SerialCommunicationChannel.DATABITS_8,
                SerialCommunicationChannel.PARITY_NONE,
                SerialCommunicationChannel.STOPBITS_1
        );
    }

    public void setProtocol(int protocol) {
        this.protocol = protocol;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public void enableDataReadout(boolean enabled) {
        this.dataReadOut = enabled;
    }

    public byte[] getDataReadout() {
        return new byte[0];
    }

    public String getReceivedIdent() {
        return receivedIdent;
    }

    private InputStream getInputStream() throws IOException {
        InputStream inputStream = getCommunicationChannel().getInputStream();
        if (inputStream == null) {
            throw new IOException("InputStream can not be 'null'.");
        } else {
            return inputStream;
        }
    }

    private OutputStream getOutputStream() throws IOException {
        OutputStream outputStream = getCommunicationChannel().getOutputStream();
        if (outputStream == null) {
            throw new IOException("OutputStream can not be 'null'.");
        } else {
            return outputStream;
        }
    }

    private SerialCommunicationChannel getCommunicationChannel() {
        return communicationChannel;
    }

    private void writeRawData(byte[] bytes) throws IOException {
        byte[] bytesToWrite = bytes.clone();
        for (int i = 0; i < bytesToWrite.length; i++) {
            if (isOddParity(bytesToWrite[i])) {
                bytesToWrite[i] |= 0x080;
            }
        }
        getOutputStream().write(bytesToWrite);
        getOutputStream().flush();
    }

    public static boolean isOddParity(final byte b) {
        final int bb = b & 0xff;
        int parity = bb ^ (bb >> 4);
        parity ^= parity >> 2;
        parity ^= parity >> 1;
        return (parity & 1) != 0;
    }

}
