package com.energyict.protocolimpl.EMCO;

import com.energyict.cbo.NestedIOException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.EMCO.frame.ErrorResponseFrame;
import com.energyict.protocolimpl.EMCO.frame.RegisterResponseFrame;
import com.energyict.protocolimpl.EMCO.frame.RequestFrame;
import com.energyict.protocolimpl.EMCO.frame.ResponseFrame;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Used to send / receive requests.
 * <p/>
 * Copyrights EnergyICT
 * User: sva
 * Date: 22/02/12
 * Time: 14:25
 */
public class FP93Connection implements ProtocolConnection {

    private FP93 meterProtocol;
    private InputStream inputStream;
    private OutputStream outputStream;
    private int timeout, retries;

    public FP93Connection(FP93 meterProtocol, InputStream inputStream, OutputStream outputStream, int timeoutProperty, int protocolRetriesProperty) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        this.timeout = timeoutProperty;
        this.retries = protocolRetriesProperty;
        this.meterProtocol = meterProtocol;
    }

    /**
     * Setter for the HHU (hand held unit) specific methods. No implementation in most cases.
     */
    public void setHHUSignOn(HHUSignOn hhuSignOn) {
    }

    /**
     * Getter for the HHU (hand held unit) specific methods. No implementation in most cases.
     */
    public HHUSignOn getHhuSignOn() {
        return null;
    }

    /**
     * Implements the specific meter communication disconnect
     */
    public void disconnectMAC() throws NestedIOException, ProtocolConnectionException {
        // no specific MAC disconnect needed.
    }

    /**
     * Implements the specific meter communication connect
     */
    public MeterType connectMAC(String strID, String strPassword, int securityLevel, String nodeId) throws IOException, ProtocolConnectionException {
        return null;  //No specific MAC connect needed.
    }

    /**
     * Implements the dataReadout functionality. Specific IEC1107 protocol related.
     */
    public byte[] dataReadout(String strID, String nodeId) throws NestedIOException, ProtocolConnectionException {
        return new byte[0];
    }

    /**
     * Write the request to the outputStream
     *
     * @param data
     * @throws IOException
     */
    private void sendRequest(byte[] data) throws IOException {
        outputStream.write(data);
    }

    /**
     * Try to read out a complete response frame, timeout if no complete response is received in the defined amount of time.
     * When the response frame contains the 'fault are present' char, a retry is issued.
     *
     * @param retryRequest: the request to be executed when retrying.
     * @return the read byte
     * @throws java.io.IOException when there's a communication problem
     */
    public byte[] readResponse(RequestFrame retryRequest) throws IOException {
        long endMillis = System.currentTimeMillis() + timeout;
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        int counter = 0;
        int previousKar = 0;
        int kar;

        while (true) {
            try {
                if (inputStream.available() > 0) {
                    kar = inputStream.read();
                    byteStream.write(kar);

                    // Response should end with ASCII carriage return followed by a ASCII line feed.
                    if ((previousKar == 0x0D) && (kar == 0x0A)) {
                        // Check the crc byte
                        verifyCheckSum(byteStream.toByteArray());

                        // Check if the response message is an error message
                        byte[] bytes = byteStream.toByteArray();
                        if (checkResponseType(bytes, '*')) {
                            ErrorResponseFrame frame = new ErrorResponseFrame();
                            frame.parseBytes(bytes);
                            throw new ProtocolConnectionException("Device responded with an error message - " + frame.getErrorMessage() + ".",
                                    Integer.toString(frame.getErrorCode()));
                        } else {
                            return bytes;
                        }
                    }
                    previousKar = kar;
                }else {
                    Thread.sleep(1);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw ConnectionCommunicationException.communicationInterruptedException(e);
            } catch (ProtocolConnectionException e) {
                if (counter == retries) {
                    throw new ProtocolConnectionException(e.getMessage() + ", after " + retries + " retries", e.getProtocolErrorCode());
                } else {
                    sendRequest(retryRequest.getBytes());
                    endMillis = System.currentTimeMillis() + timeout;
                    counter++;
                }
            }

            // in case of a response timeout
            if (System.currentTimeMillis() > endMillis) {
                if (counter == retries) {
                    throw new ProtocolConnectionException("Response timeout error, after " + retries + " retries");
                } else {
                    sendRequest(retryRequest.getBytes());
                    endMillis = System.currentTimeMillis() + timeout;
                    counter++;
                }
            }
        }
    }

    /**
     * Send out the request to the meter and retrieve the response.
     */
    public ResponseFrame sendAndReceiveResponse(RequestFrame request) throws IOException {
        // 1. Send out the request
        sendRequest(request.getBytes());

        // 2. Retrieve the complete response frame
        byte[] bytes = readResponse(request);

        // 3. Load the response bytes into a proper response frame.
        ResponseFrame responseFrame;
        if (checkResponseType(bytes, '#')) {
            responseFrame = new RegisterResponseFrame();
            responseFrame.parseBytes(bytes);
            responseFrame.checkMatchingRequest(request);
        } else {
            throw new ProtocolConnectionException("Expected a response message of type register (#), but was of type " + (char) bytes[6]);
        }

        return responseFrame;
    }

    public static void verifyCheckSum(byte[] bytes) throws ProtocolConnectionException {
        byte[] checkSumBytes = ProtocolTools.getSubArray(bytes, bytes.length - 4, bytes.length - 2);
        byte responseChecksum = (byte) Integer.parseInt(ProtocolTools.getAsciiFromBytes(checkSumBytes), 16);

        byte calculatedChecksum = 0;
        byte[] responseFrame = ProtocolTools.getSubArray(bytes, 0, bytes.length - 4);

        for (byte responseByte : responseFrame) {
            calculatedChecksum += responseByte;
        }

        if (responseChecksum != calculatedChecksum) {
            throw new ProtocolConnectionException("The checksum of the response frame is incorrect.");

        }
    }

    private boolean checkResponseType(byte[] response, char type) {
        return ((response[4] == type) || (response[5] == type) || (response[6] == type));
    }
}