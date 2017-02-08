package com.energyict.protocolimpl.coronis.core.wavecell;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.mdc.upl.ProtocolException;
import com.energyict.protocol.exception.ConnectionCommunicationException;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.coronis.core.WaveFlowConnect;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Logger;

/**
 * Connection that encapsulates Wavenis RF frames and sends them to a WaveCell gateway.
 */
public class WaveCellConnect extends WaveFlowConnect {

    private InputStream inputStream;
    private OutputStream outputStream;

    private static final int ACK_LENGTH = 4;
    private static final String SPACE = " ";
    private static final String OPENING_BRACKET_BYTE = "[";
    private static final String CLOSING_BRACKET_BYTE = "]";
    private static final String COMMAND_START_BYTE = "<";
    private static final String COMMAND_CLOSE_BYTE = ">";
    private static final byte[] INI_ACK = "ACK\r\n".getBytes();
    private static final byte[] CRC = new byte[]{0, 0};
    private static final int MESS_COUNT = 0x11;
    private static final int HEADER_LENGTH = 10;
    private static final int REQUEST_OPCODE = 0x04;
    private static final int CR = 0x0D;
    private static final int NL = 0x0A;
    private byte[] request = null;

    private int opCount = 0;
    private int txCounter = 0;
    private int rxCounter = 0;
    private int txFrameCounter = 0;
    private int rxFrameCounter = 0;

    public WaveCellConnect(final InputStream inputStream, final OutputStream outputStream, final int timeout, final Logger logger, final long forceDelay, final int retries) throws ConnectionException {
        super(inputStream, outputStream, timeout, logger, forceDelay, retries);
        this.inputStream = inputStream;
        this.outputStream = outputStream;
        resetCounters();
    }

    private void resetCounters() {
        txCounter = 0;
        rxCounter = 0;
        txFrameCounter = 0;
        rxFrameCounter = 0;
    }

    public int getTxCounter() {
        return txCounter;
    }

    public int getRxCounter() {
        return rxCounter;
    }

    public int getTxFrameCounter() {
        return txFrameCounter;
    }

    public int getRxFrameCounter() {
        return rxFrameCounter;
    }

    /**
     * Send a request, return the response or timeout. This method is used for all WaveFlow communications!
     */
    public byte[] sendData(byte[] request) throws IOException {
        this.request = request;

        //Send
        sendRequest();

        //Receive Ack
        readAndVerifyAck();

        //Receive and acknowledge the response
        WaveCellFrame response = readAndVerifyAns();

        this.request = null;

        //Return response
        return response.getApplicativeData().getWavenisFrame();
    }

    private void sendRequest() throws IOException {
        delay(5000);       //Wait a while before sending requests
        byte[] frame = encapsulateRequest();
        sendBytes(convertBytesToAscii(frame));
    }

    private void readAndVerifyAck() throws IOException {
        WaveCellFrame waveCellFrame = readAndIgnoreEarlierFrames();
        if (!waveCellFrame.isAckCommand()) {
            throw new ProtocolException("Received an unexpected frame (type '" + waveCellFrame.getCommand() + "'), expected 'ACK' after a request");
        }
    }

    private void verifyOpCount(WaveCellFrame response) throws IOException {
        if (opCount != response.getOpCount()) {
            throw new IOException("Received frame with OP_COUNT = " + response.getOpCount() + ", expected " + opCount);
        }
    }

    private WaveCellFrame readAndVerifyAns() throws IOException {
        WaveCellFrame waveCellFrame = readAndIgnoreEarlierFrames();
        if (!waveCellFrame.isAnsCommand()) {
            throw new IOException("Received an unexpected frame (type '" + waveCellFrame.getCommand() + "'), expected 'ANS' after a request");
        }
        return waveCellFrame;
    }

    private WaveCellFrame readAndIgnoreEarlierFrames() throws IOException {
        WaveCellFrame waveCellFrame = readInboundFrame();
        while (waveCellFrame.isIniCommand() || waveCellFrame.isReadyCommand()) {
            waveCellFrame = readInboundFrame();    //Ignore the RDY and INI frames, read in the next frame
        }
        while (waveCellFrame.getOpCount() < opCount) {
            warning("Received a late response to an earlier request, ignoring this frame.");
            waveCellFrame = readInboundFrame();    //Ignore earlier responses that arrive late, wait for the correct frame
        }
        verifyOpCount(waveCellFrame);
        return waveCellFrame;
    }

    private byte[] convertBytesToAscii(byte[] frame) {
        return ProtocolTools.getHexStringFromBytes(frame, "").getBytes();
    }

    private byte[] encapsulateRequest() {
        increaseOpCount();
        byte[] frame = ProtocolTools.concatByteArrays(createHeader(), request);
        return ProtocolTools.concatByteArrays(frame, CRC);
    }

    private byte[] createHeader() {
        byte[] header = new byte[9];
        header[0] = getFrameLength(request);
        header[1] = getFrameLength(request);
        header[2] = (byte) 0x11;
        header[3] = (byte) REQUEST_OPCODE;
        header[4] = (byte) opCount;
        header[5] = (byte) 1;         //1 message
        header[6] = (byte) ((getWaveFlowId() >> 8) & 0xFF);
        header[7] = (byte) (getWaveFlowId() & 0xFF);
        header[8] = (byte) request.length;
        return header;
    }

    private byte getFrameLength(byte[] request) {
        return (byte) (request.length + HEADER_LENGTH);
    }

    private int increaseOpCount() {
        opCount++;
        if (opCount > 0xFF) {
            opCount = 1;
        }
        return opCount;
    }

    @Override
    public MeterType connectMAC(String strID, String strPassword, int securityLevel, String nodeId) throws IOException {
        return null;
    }

    private void sendBytes(byte[] request) throws IOException {
        txFrameCounter++;
        txCounter += request.length;
        outputStream.write(request);
    }

    /**
     * Try to read a number of bytes, timeout if nothing is received for a defined amount of time.
     *
     * @param length the number of bytes to be read
     * @return the read byte
     * @throws java.io.IOException when there's a communication problem
     */
    private byte[] readBytes(int length) throws IOException {
        int timeout = this.timeout;

        long endMillis = System.currentTimeMillis() + timeout;
        int counter = 0;

        while (true) {

            try {
                if (inputStream.available() >= length) {
                    byte[] result = new byte[length];
                    inputStream.read(result);
                    rxCounter += result.length;
                    return result;
                } else {
                    Thread.sleep(1);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw ConnectionCommunicationException.communicationInterruptedException(e);
            } catch (IOException e) {
                throw new ConnectionException("Connection, readBytes() error " + e.getMessage());
            }

            // in case of a response timeout
            if (System.currentTimeMillis() > endMillis) {
                if (request == null) {
                    throw new ProtocolConnectionException("Received incomplete frame... (1 or more bytes missing)");
                } else {
                    if (counter == retries) {
                        throw new ProtocolConnectionException("Response timeout error, after " + retries + " retries", TIMEOUT_ERROR);
                    } else {   //Retry
                        counter++;
                        info("Request timed out after " + timeout + " ms, retrying (" + counter + "/" + retries + ") with new timeout: " + (timeout + this.timeout) + " ms.");
                        timeout += this.timeout;    //Increase the timeout
                        sendRequest();
                        endMillis = System.currentTimeMillis() + timeout;
                    }
                }
            }
        }
    }

    /**
     * Try to read one byte, timeout or retry if nothing is received for a defined amount of time.
     *
     * @return the read byte
     * @throws java.io.IOException when there's a communication problem
     */
    private int readByte() throws IOException {
        return readBytes(1)[0] & 0xFF;
    }

    /**
     * Reads in a response from the WaveCell, parses the header and returns the frame.
     *
     * @return applicative data or null if there's none
     * @throws IOException
     */
    public WaveCellFrame readInboundFrame() throws IOException {
        WaveCellFrame response = readWaveCellHeader();  //To check the WaveCell ID and the command

        if (response.isIniCommand()) {
            readFrameEnd();
            info("Received initialization frame from the WaveCell");
            acknowledgeInitialize();
        } else if (response.isReadyCommand()) {
            readFrameEnd();
            info("Received periodic authentication from the WaveCell");
        } else if (response.isAckCommand()) {
            response.setOpCount(readOctet());
            readFrameEnd();
        } else if (response.isError()) {
            readFrameEnd();
            throw new ConnectionException("Request failed, received 'ERR' response... possibly wrong request or wrong CRC.");
        } else if (response.isAnsCommand()) { //Response or alarm
            readAndVerifyCharacter(SPACE);
            response = readWaveCellFrame(response);
            readFrameEnd();
            sendAck(response);
            if (response.getApplicativeData().getWavenisFrame().length == 0) {
                throw new ConnectionException("Received empty RF frame, radio command or parameter probably not supported...");
            }
        } else {
            throw new ConnectionException("Received an unexpected command: '" + response.getCommand() + "'. Expected INI, RDY, ANS, ERR or ACK.");
        }
        return response;
    }

    private void readFrameEnd() throws IOException {
        readAndVerifyCharacter(CR);
        readAndVerifyCharacter(NL);
    }

    /**
     * Read in a full WaveCell frame
     *
     * @throws IOException due to a timeout
     */
    private WaveCellFrame readWaveCellFrame(WaveCellFrame response) throws IOException {
        int length = readAndCheckLength();
        readAndCheckMessCount();
        response.setOpCode(readOctet());
        response.setOpCount(readOctet());
        byte[] applicativeData = readOctets(getApplicativeDataLength(length));
        response.setApplicativeData(applicativeData);
        byte[] crcBytes = readOctets(2);
        int crc = ProtocolTools.getUnsignedIntFromBytes(crcBytes);
        response.setCrc(crc);
        return response;
    }

    private void sendAck(WaveCellFrame response) throws IOException {
        byte[] ack = new byte[ACK_LENGTH + 1];
        ack[0] = (byte) ACK_LENGTH;
        ack[1] = (byte) ACK_LENGTH;
        ack[2] = (byte) response.getOpCount();
        ack[3] = (byte) (0x00);
        ack[4] = (byte) (0x00);

        sendBytes(convertBytesToAscii(ack));
    }

    public void acknowledgeInitialize() throws IOException {
        sendBytes(INI_ACK);
    }

    private int getApplicativeDataLength(int length) {
        return length - 6;      //Total frame length - status bytes (2) - opCode byte - opCount byte - crc bytes (2)
    }

    private void readAndCheckMessCount() throws IOException {
        int messCount = readOctet();
        if (messCount != MESS_COUNT) {
            throw new IOException("Unexpected Mess_count, expected 17, received " + messCount);
        }
    }

    private int readAndCheckLength() throws IOException {
        int length = readOctet();
        int length2 = readOctet();
        if (length != length2) {
            throw new IOException("Unexpected frame length confirmation, expected " + length + ", received " + length2);
        }
        return length;
    }

    /**
     * Reads in 2 actual bytes, representing the ascii notation of a hex byte.
     * E.g. reading [0x30 0x31] returns byte 01
     *
     * @throws IOException timeout
     */
    private int readOctet() throws IOException {
        return ProtocolTools.getBytesFromHexString(readCharacters(2), "")[0] & 0xFF;
    }

    private byte[] readOctets(int length) throws IOException {
        byte[] response = readBytes(length * 2);
        String ascii = getASCIIFromBytes(response);
        return ProtocolTools.getBytesFromHexString(ascii, "");
    }

    /**
     * Format is [WaveCellID] <CMD>
     *
     * @return CMD
     */
    private WaveCellFrame readWaveCellHeader() throws IOException {
        readAndVerifyCharacter(OPENING_BRACKET_BYTE);
        rxFrameCounter++;
        String waveCellID = readWaveCellID();
        readAndVerifyCharacter(SPACE);
        readAndVerifyCharacter(COMMAND_START_BYTE);
        String command = readCharacters(3);
        readAndVerifyCharacter(COMMAND_CLOSE_BYTE);
        return new WaveCellFrame(command, waveCellID);
    }

    private String readWaveCellID() throws IOException {
        StringBuilder stringBuilder = new StringBuilder();
        for (int count = 0; count < 11; count++) {          //Max length of the received WaveCell ID is 10 characters
            int idCharacter = readByte();
            if (idCharacter == (CLOSING_BRACKET_BYTE.getBytes()[0] & 0xFF)) {      //Indicates end of the wave cell ID
                break;
            }
            stringBuilder.append(getASCIIFromInt(idCharacter));
        }
        return stringBuilder.toString();
    }

    private String getASCIIFromInt(int idCharacter) {
        return getASCIIFromByte((byte) idCharacter);
    }

    private String getASCIIFromByte(byte b) {
        return new String(new byte[]{b});
    }

    private String getASCIIFromBytes(byte[] bytes) {
        return new String(bytes);
    }

    private int readAndVerifyCharacter(String expectedCharacter) throws IOException {
        return readAndVerifyCharacter(expectedCharacter.getBytes()[0] & 0xFF);
    }

    private int readAndVerifyCharacter(int expectedCharacter) throws IOException {
        int receivedCharacter = readByte();
        if (receivedCharacter != expectedCharacter) {
            throw new ProtocolException("Unexpected character '" + receivedCharacter + "' in the frame, expected '" + expectedCharacter + "'. Format should be [ID] <CMD> Message");
        }
        return receivedCharacter;
    }

    private String readCharacters(int length) throws IOException {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(getASCIIFromInt(readByte()));
        }
        return sb.toString();
    }

    protected void warning(String msg) {
        getLogger().warning(msg);
    }

    protected void severe(String msg) {
        getLogger().severe(msg);
    }

    protected void info(String msg) {
        getLogger().info(msg);
    }
}