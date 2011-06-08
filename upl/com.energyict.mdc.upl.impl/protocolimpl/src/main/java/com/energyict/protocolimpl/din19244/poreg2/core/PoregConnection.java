package com.energyict.protocolimpl.din19244.poreg2.core;

import com.energyict.cbo.NestedIOException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.base.*;
import com.energyict.protocolimpl.din19244.poreg2.Poreg;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.*;
import java.util.Arrays;

/**
 * Used to send / receive requests, or to dis/connect to the device.
 *
 * Copyrights EnergyICT
 * Date: 19-apr-2011
 * Time: 15:08:41
 */
public class PoregConnection implements ProtocolConnection {

    private InputStream inputStream;
    private OutputStream outputStream;
    private int timeout;
    private int retries;
    private Poreg poreg;

    private static final int END = 0x16;
    private static final int START_FIXED = 0x10;
    private static final int START_VARIABLE = 0x68;
    private static final int START_CONNECT = 0x43;
    private static final int CONTINUE = 0x40;
    private static final String CONNECTED = "C";
    private boolean doContinue = false;

    public PoregConnection(Poreg poreg, InputStream inputStream, OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty, int forcedDelay, int echoCancelling, int protocolCompatible, Encryptor encryptor, HalfDuplexController halfDuplexController) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.timeout = timeoutProperty;
        this.retries = protocolRetriesProperty;
        this.poreg = poreg;
    }

    public void setHHUSignOn(HHUSignOn hhuSignOn) {
    }

    public boolean isDoContinue() {
        return doContinue;
    }

    public HHUSignOn getHhuSignOn() {
        return null;
    }

    public void disconnectMAC() throws NestedIOException, ProtocolConnectionException {
        try {
            byte[] result = sendAndReceive(Poreg2Frame.getDisconnectFrame(poreg));
            parseFixedFrame(result, Response.ACK);
        } catch (IOException e) {
            throw new NestedIOException(e);
        }
    }

    /*
    A fixed frame only contains a response status and an address.
     */

    private void parseFixedFrame(byte[] result, Response expected) throws IOException {
        int response = result[0] & 0xFF;
        if (response != expected.getId()) {
            throw new IOException("Error disconnecting, expected ACK, received " + response);
        }
        byte[] address = ProtocolTools.getSubArray(result, 1, 3);
        if (!Arrays.equals(getAddressBytes(poreg.getStrID()), address)) {
            throw new IOException("Error disconnecting, received wrong address");
        }
    }

    /**
     * Try to read one byte, timeout if nothing is received for a defined amount of time.
     *
     * @param retryRequest: the request to be executed when retrying.
     * @return the read byte
     * @throws java.io.IOException when there's a communication problem
     */
    public int readByte(byte[] retryRequest) throws IOException {
        return readBytes(1, retryRequest)[0] & 0xFF;
            }

    /**
     * Try to read a number of bytes, timeout if nothing is received for a defined amount of time.
     *
     * @param length the number of bytes to be read
     * @param retryRequest: the request to be executed when retrying.
     * @return the read byte
     * @throws java.io.IOException when there's a communication problem
     */
    public byte[] readBytes(int length, byte[] retryRequest) throws IOException {

        long endMillis = System.currentTimeMillis() + timeout;
        int counter = 0;

        while (true) {

        try {
                if (inputStream.available() >= length) {
                byte[] result = new byte[length];
                inputStream.read(result);
                return result;
            } else {
                Thread.sleep(1);
            }
        } catch (InterruptedException e) {
            throw new NestedIOException(e);
        } catch (IOException e) {
            throw new ConnectionException("Connection, readBytes() error " + e.getMessage());
        }

            // in case of a response timeout
            if (System.currentTimeMillis() > endMillis) {
                if (counter == retries) {
                    throw new ProtocolConnectionException("Response timeout error, after " + retries + " retries");
                } else {
                    sendData(retryRequest);
                    endMillis = System.currentTimeMillis() + timeout;
                    counter++;
    }
            }
        }
    }

    public byte[] sendAndReceive(byte[] data) throws IOException {
        sendData(data);
        ByteArrayOutputStream result = new ByteArrayOutputStream();

        int kar = readByte(data);
                switch (kar) {
                    case START_FIXED:
                readFixedFrame(result, data);
                        return result.toByteArray();
                    case START_VARIABLE:
                readVariableFrame(result, data);
                        return result.toByteArray();
                    case START_CONNECT:
                        result.write(kar);
                readConnectionFrame(result, data);
                        return result.toByteArray();
                }
        throw new ProtocolConnectionException("Unexpected first byte: " + kar + ". Expected: 0x10 (fixed frame), 0x68 (variable frame) or 0x43 (connecting)");

                }

    private void sendData(byte[] data) throws IOException {
        outputStream.write(data);
    }

    private void readConnectionFrame(ByteArrayOutputStream result, byte[] retryRequest) throws IOException {
        byte[] userData;
        userData = readBytes(9, retryRequest);
        result.write(userData);
    }

    private void readVariableFrame(ByteArrayOutputStream result, byte[] retryRequest) throws IOException {
        byte[] userData;
        int length = readByte(retryRequest);
        int length2 = readByte(retryRequest);
        if (length != length2) {
            throw new ProtocolConnectionException("Connection frame error: expected " + length + " as length confirmer, received " + length2);
        }
        int start2 = readByte(retryRequest);
        if (START_VARIABLE != start2) {
            throw new ProtocolConnectionException("Connection frame error: expected " + START_VARIABLE + " as start confirmer, received " + start2);
        }
        if (length > 0) {
            userData = readBytes(length, retryRequest);
        } else {
            return;
        }
        int crc = readByte(retryRequest);
        int calcedCRC = calcCRC(userData);
        if (crc != calcedCRC) {
            throw new ProtocolConnectionException("CRC error: expected " + calcedCRC + ", received " + crc);
        }
        int end = readByte(retryRequest);
        if (end != END) {
            throw new ProtocolConnectionException("Connection error: expected " + END + "at the end, received " + end);
        }
        result.write(userData);
    }

    private int calcCRC(byte[] userData) {
        return CRCGenerator.getModulo256(userData);
    }

    private void readFixedFrame(ByteArrayOutputStream result, byte[] retryRequest) throws IOException {
        byte[] userData = readBytes(3, retryRequest);
        int crc = readByte(retryRequest);
        int calcedCRC = calcCRC(userData);
        if (crc != calcedCRC) {
            throw new ProtocolConnectionException("CRC error: expected " + calcedCRC + ", received " + crc);
        }
        int end = readByte(retryRequest);
        if (end != END) {
            throw new ProtocolConnectionException("Connection frame error: expected " + END + " at the end, received " + end);
        }
        result.write(userData);
    }

    public MeterType connectMAC(String strID, String strPassword, int securityLevel, String nodeId) throws IOException {
        if (strID == null || "".equals(strID)) {
            strID = "00000000";
        }
        if (strPassword == null) {
            strPassword = "";
        }
        if (nodeId == null || "".equals(nodeId)) {
            nodeId = "00000000";
        }
        byte[] data = Poreg2Frame.getConnectFrame(strID, strPassword, nodeId);
        byte[] result = sendAndReceive(data);
        String response = new String(result).substring(0, 1);
        if (!CONNECTED.equals(response)) {
            throw new IOException("Error connecting to the Poreg 2 data recorder, returned " + response);
        }
        return new MeterType("Poreg");
    }

    public byte[] dataReadout(String strID, String nodeId) throws NestedIOException, ProtocolConnectionException {
        return new byte[0];
    }

    public byte[] doRequest(byte[] requestASDUType, byte[] extraInfo, int expectedResponseType, int expectedASDUType) throws IOException {
        byte[] request = Poreg2Frame.getRequestFrame(poreg, requestASDUType, extraInfo);
        byte[] response = sendAndReceive(request);
        return parseResponseHeader(response, expectedResponseType, expectedASDUType);
    }

    /**
     * Do a request with a simple fixed response.
     */
    public byte[] doSimpleRequest(byte[] requestASDUType, byte[] header, byte[] extraInfo) throws IOException {
        byte[] request = Poreg2Frame.getRequestFrame(poreg, requestASDUType, ProtocolTools.concatByteArrays(header, extraInfo));
        return sendAndReceive(request);
    }

    private byte[] parseResponseHeader(byte[] response, int expectedResponseType, int expectedASDUType) throws IOException {
        int offset = 0;

        int type = response[offset] & 0xFF;
        if (type != expectedResponseType) {
            throw new IOException("Unexpected data type, received " + Response.getDescription(type) + ", expected " + Response.getDescription(expectedResponseType));
        }
        offset++;

        byte[] address = ProtocolTools.getSubArray(response, offset, offset + 2);
        if (!Arrays.equals(address, getAddressBytes(poreg.getStrID()))) {
            throw new IOException("Error receiving register group, unexpected device address");
        }
        offset += 2;

        int asdu = response[offset] & 0xFF;
        if (asdu == 0) {
            throw new IOException("Error requesting data. Wrong password?");
        }

        if (asdu != expectedASDUType) {
            throw new IOException("Error receiving register group, expected ASDU type " + expectedASDUType + "in the response, got " + asdu);
        }
        offset++;

        int length = response[offset] & 0xFF;
        if (length != (response.length - 3)) {
            throw new IOException("Error receiving register group, length mismatch. Expected " + (response.length - 3) + ", received " + length);
        }
        offset++;

        doContinue = ((response[offset] & 0xFF) == CONTINUE);  //Check if a following frame is expected

        offset += 4; //Skip the 4 zero bytes.

        return ProtocolTools.getSubArray(response, offset);
    }

    private byte[] getAddressBytes(String address) {
        byte[] hex = ProtocolTools.getBytesFromInt(Integer.parseInt(address), 3);
        return new byte[]{hex[2], hex[1]};
    }

    public byte[] doContinue(int expectedResponseType, int expectedASDUType) throws IOException {
        byte[] request = Poreg2Frame.getContinueFrame(poreg);
        byte[] response = sendAndReceive(request);
        return parseResponseHeader(response, expectedResponseType, expectedASDUType);
    }
}
