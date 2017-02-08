package com.elster.us.protocolimplv2.mercury.minimax;

import com.elster.us.protocolimplv2.mercury.minimax.frame.RequestFrame;
import com.elster.us.protocolimplv2.mercury.minimax.frame.ResponseFrame;
import com.elster.us.protocolimplv2.mercury.minimax.frame.data.BasicData;
import com.elster.us.protocolimplv2.mercury.minimax.frame.data.ExtendedData;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.mdc.upl.io.NestedIOException;
import com.energyict.protocol.exception.ConnectionCommunicationException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import static com.elster.us.protocolimplv2.mercury.minimax.Command.RD;
import static com.elster.us.protocolimplv2.mercury.minimax.Command.RG;
import static com.elster.us.protocolimplv2.mercury.minimax.Command.SF;
import static com.elster.us.protocolimplv2.mercury.minimax.Command.SN;
import static com.elster.us.protocolimplv2.mercury.minimax.Command.WD;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.CONTROL_ACK;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.CONTROL_ENQ;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.CONTROL_EOT;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.CONTROL_ETX;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.CONTROL_RS;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.CONTROL_SOH;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.CONTROL_STX;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.ERROR_INVALID_ENQUIRY;
import static com.elster.us.protocolimplv2.mercury.minimax.Consts.STR_VQ;
import static com.elster.us.protocolimplv2.mercury.minimax.utility.ByteArrayHelper.arraysEqual;
import static com.elster.us.protocolimplv2.mercury.minimax.utility.ByteArrayHelper.getBytes;
import static com.elster.us.protocolimplv2.mercury.minimax.utility.ByteArrayHelper.getString;

/**
 * Deals with the connection-level communications with the Mercury EVC
 *
 * @author James Fox
 */
public class MiniMaxConnection {

    private static final int CRC_LENGTH = 4;
    private final MiniMaxProperties properties;

    private boolean connected = false;
    private Logger logger;
    private Command lastCommandSent;
    private ComChannel channel;

    public MiniMaxConnection(ComChannel channel, MiniMaxProperties properties, Logger logger) {
        this.properties = properties;
        this.logger = logger;
        this.channel = channel;
    }

    protected Logger getLogger() {
        return logger;
    }

    public void setLastCommandSent(Command sent) {
        this.lastCommandSent = sent;
    }

    public List<ResponseFrame> sendAndReceiveFrames(RequestFrame toSend) throws IOException {
        sendFrame(toSend);
        return receiveResponseFrames();
    }

    private ResponseFrame sendAndReceiveFrame(RequestFrame toSend) throws IOException {
        List<ResponseFrame> responseFrames = sendAndReceiveFrames(toSend);
        if (responseFrames.size() == 1) {
            return responseFrames.get(0);
        } else {
            throw new IOException("Unexpected number of response frames received: " + responseFrames.size() + ", expecting 1");
        }
    }

    private ResponseFrame sendSignOn() throws IOException {
        // Construct the command params
        ByteArrayOutputStream commandParams = new ByteArrayOutputStream();
        // Always send a comma char here according to spec.
        commandParams.write(',');
        // ACS - access code password
        byte[] pwdBytes = getBytes(properties.getDevicePassword());
        commandParams.write(pwdBytes);
        commandParams.write(CONTROL_STX);
        // Always send vq here for sign on
        commandParams.write(getBytes(STR_VQ));
        // device code
        byte[] deviceIdBytes = getBytes(properties.getDeviceId());
        commandParams.write(deviceIdBytes);
        // Construct the data
        ExtendedData data = new ExtendedData(SN.name().getBytes(), commandParams.toByteArray());
        // Construct the frame, including the data
        RequestFrame snFrame = new RequestFrame(data);
        // Keep a record of the last command we sent to the device
        lastCommandSent = SN;
        // Send the frame and handle the response
        return sendAndReceiveFrame(snFrame);
    }

    private ResponseFrame sendSignOff() throws IOException {
        BasicData data = new BasicData(getBytes(SF.name()));
        RequestFrame snFrame = new RequestFrame(data);
        lastCommandSent = SF;
        return sendAndReceiveFrame(snFrame);
    }

    private void sendFrame(RequestFrame frame) throws IOException {
        byte[] toSend = frame.toByteArray();
        channel.startWriting();
        channel.write(toSend);
    }

    private List<ResponseFrame> receiveResponseFrames() throws IOException {
        return receiveResponseFrames(false);
    }

    private List<ResponseFrame> receiveResponseFrames(boolean sohAlreadyReceived) throws IOException  {

        List<ResponseFrame> receivedFrames = new ArrayList<>();

        byte b = 0x00;

        if (!sohAlreadyReceived) {
            // Find the start of the sequence of frames
            channel.startReading();
            while (b != CONTROL_SOH) {
                b = (byte)channel.read();
                logger.info("discarding byte " + b);
                if (b == -1) {
                    throw new IOException("End of stream when reading from device");
                }
            }
        }

        ByteArrayOutputStream receivedBytes = new ByteArrayOutputStream();
        while (b != CONTROL_EOT) {
            channel.startReading();
            b = (byte)channel.read();
            if (b == -1) {
                throw new IOException("End of stream when reading from device");
            }

            if (b == CONTROL_RS) {
                // Receiving multiple packets...
                // send ack
                channel.startWriting();
                channel.write(CONTROL_ACK);
                // Clear received bytes
                logger.info("Resetting receivedBytes " + receivedBytes.size() + ", " + new String(receivedBytes.toByteArray()));
                receivedBytes.reset();
                channel.startReading();
                b = (byte)channel.read();
            }

            if (b != CONTROL_ETX) {
                receivedBytes.write(b);
            } else {
                byte[] crc = readCrc();
                logger.info("Creating response frame with bytes length " + receivedBytes.size() + ", " + new String(receivedBytes.toByteArray()));
                ResponseFrame response = new ResponseFrame(receivedBytes.toByteArray(), lastCommandSent, getLogger());
                if (checkCrc(response.generateCRC(), crc)) {
                    // add the frame to be returned
                    receivedFrames.add(response);
                } else {
                    throw new IOException("CRC check failed");
                }
            }
        }
        return receivedFrames;
    }

    private boolean checkCrc(byte[] receivedBytes, byte[] crc) {
        return arraysEqual(receivedBytes, crc);
    }

    private byte[] readCrc() throws IOException {
        // Read the next 4 bytes from the InputStream
        byte[] crcBytes = new byte[CRC_LENGTH];
        channel.startReading();
        int bytesRead = channel.read(crcBytes);
        if (bytesRead != CRC_LENGTH) {
            throw new IOException("Failed to read CRC from input stream");
        }
        return crcBytes;
    }

    private void sendSpecialCommand(byte b) {
        channel.startWriting();
        channel.write(b);
    }

    private byte receiveSpecialCommand() {
        channel.startReading();
        return (byte)channel.read();
    }

    public void doConnect() {
        int numRetries = 0;
        try {
            while (!connected && numRetries < properties.getRetries()) {
                // Send EOT
                sendSpecialCommand(CONTROL_EOT);
                // Wait 1 second
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ie) {
                    throw ConnectionCommunicationException.communicationInterruptedException(ie);
                }
                // Send ENQ
                sendSpecialCommand(CONTROL_ENQ);

                // Read response
                byte received = receiveSpecialCommand();

                if (received == CONTROL_ACK) {
                    // We received an ACK, we can send an SN packet
                    if (doSendSignOn()) {
                        break;
                    }
                } else {
                    if (received == CONTROL_SOH) {
                        // We've received a response, could be an indication that we can go ahead and send a sign-on
                        List<ResponseFrame> responseFrames = receiveResponseFrames(true);
                        for (ResponseFrame response : responseFrames) {
                            if (getString(response.getData().getErrorCode()).equals(ERROR_INVALID_ENQUIRY)) {
                                // Invalid enquiry means that we can send the sign-on
                                if (doSendSignOn()) {
                                    break;
                                }
                            }
                        }
                    }
                    numRetries++;
                    // Wait a second then go around again...
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ie) {
                        throw new NestedIOException(ie);
                    }
                }
            }
        } catch (IOException ioe) {
            throw ConnectionCommunicationException.protocolConnectFailed(ioe);
        }
    }

    private boolean doSendSignOn() throws IOException {
        ResponseFrame signOnResponse = sendSignOn();
        if (signOnResponse.isOK()) {
            connected = true;
            return true;
        } else {
            //throw new IOException(signOnResponse.getError());
            getLogger().warning("Failed to sign on with error " + signOnResponse.getError() + ", will be retried");
            return false;
        }
    }

    public void doDisconnect() {
        try {
            ResponseFrame signOffResponse = sendSignOff();
            connected = false;
            if (!signOffResponse.isOK()) {
                throw new IOException(signOffResponse.getError());
            }
        } catch (IOException ioe) {
            throw ConnectionCommunicationException.protocolDisconnectFailed(ioe);
        }
    }

    public ResponseFrame readMultipleRegisterValues(List<String> registers) {
        ByteArrayOutputStream requestParams = new ByteArrayOutputStream();
        requestParams.write(CONTROL_STX);
        int count = 0;

        for (String str : registers) {
            try {
                requestParams.write(getBytes(str));
            } catch (IOException ioe) {
                // There is no reason for this to happen - ignore
                getLogger().warning("IOException in readMultipleRegisterValues");
            }
            if (count < registers.size() - 1) {
                requestParams.write(',');
                count++;
            }
        }
        byte[] requestParamBytes = requestParams.toByteArray();
        byte[] rgBytes = getBytes(RG.name());

        try {
            ExtendedData data = new ExtendedData(rgBytes, requestParamBytes);
            RequestFrame toSend = new RequestFrame(data);
            lastCommandSent = RG;
            return sendAndReceiveFrame(toSend);
        } catch (IOException ioe) {
            throw ConnectionCommunicationException.unExpectedProtocolError(ioe);
        }
    }

    public ResponseFrame readSingleRegisterValue(String register) {
        try {
            ByteArrayOutputStream requestParams = new ByteArrayOutputStream();
            requestParams.write(CONTROL_STX);
            requestParams.write(getBytes(register));
            ExtendedData data = new ExtendedData(getBytes(RD.name()), requestParams.toByteArray());
            RequestFrame toSend = new RequestFrame(data);
            lastCommandSent = RD;
            return sendAndReceiveFrame(toSend);
        } catch (IOException ioe) {
            throw ConnectionCommunicationException.unExpectedProtocolError(ioe);
        }
    }

    public ResponseFrame writeSingleRegisterValue(String register, String toWrite) {
        try {
            ByteArrayOutputStream requestParams = new ByteArrayOutputStream();
            requestParams.write(',');
            requestParams.write(getBytes(properties.getDevicePassword()));
            requestParams.write(CONTROL_STX);
            requestParams.write(getBytes(register));
            requestParams.write(',');
            requestParams.write(getBytes(toWrite));

            ExtendedData data = new ExtendedData(getBytes(WD.name()), requestParams.toByteArray());
            RequestFrame toSend = new RequestFrame(data);
            lastCommandSent = WD;
            return sendAndReceiveFrame(toSend);
        } catch (IOException ioe) {
            throw ConnectionCommunicationException.unExpectedProtocolError(ioe);
        }
    }
}
