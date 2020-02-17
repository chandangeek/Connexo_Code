package com.energyict.protocolimplv2.dlms.a2;

import com.energyict.dlms.DLMSConnectionException;
import com.energyict.dlms.ReceiveBuffer;
import com.energyict.dlms.protocolimplv2.CommunicationSessionProperties;
import com.energyict.dlms.protocolimplv2.connection.HDLCConnection;
import com.energyict.mdc.protocol.ComChannel;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

/**
 * @author H236365
 * addapted for use with A2 ThemisUno DLMS
 */
public class A2HHUHDLCConnection extends HDLCConnection {

    public static final String A2_THEMISUNO = "A2 ThemisUno";
    private final byte[] aarq = ProtocolTools.getBytesFromHexString("e6e600601da109060760857405080101be10040e01000000065f1f0400007e1f0100", 2);

    private A2RequestFrameBuilder frameBuilder;

    public A2HHUHDLCConnection(ComChannel comChannel, CommunicationSessionProperties properties, A2RequestFrameBuilder frameBuilder) {
        super(comChannel, properties);
        this.frameBuilder = frameBuilder;
    }

    @Override
    protected void sendInformationField(byte[] byteBuffer, boolean sendOutFrame) throws IOException, DLMSConnectionException {
        if (!boolHDLCConnected) {
            throw new DLMSConnectionException("HDLC connection not established!");
        }
        txFrame = buildRequestFrame(byteBuffer);

        if (sendOutFrame) {
            startWriting();
            sendFrame(txFrame);
        }
    }

    protected byte[] buildRequestFrame(byte[] request) {
        return frameBuilder.buildRequestFrame(request);
    }

    /**
     * Add the 0x7E at the start and the end of the frame
     */
    @Override
    public byte[] addHDLCFrameFlags(byte[] frame) {
        byte[] result = new byte[frame.length + 2];
        System.arraycopy(frame, 0, result, 1, frame.length);
        result[0] = HDLC_FLAG;
        result[result.length - 1] = HDLC_FLAG;
        return result;
    }

    /**
     * @throws DLMSConnectionException an error occurred due to e.g. unexpected data
     * @throws IOException             a communication error occurred
     */
    @Override
    protected byte[] receiveInformationField(ReceiveBuffer receiveBuffer, int currentTry) throws DLMSConnectionException, IOException {
        if (!boolHDLCConnected) {
            throw new DLMSConnectionException("HDLC connection not established!");
        }
        int nrOfConsecutiveReceiveReadyCycles = 0;

        for (this.currentTryCount = currentTry; this.currentTryCount <= iMaxRetries; this.currentTryCount++) {
            long timeOut = System.currentTimeMillis() + iProtocolTimeout;
            while (timeOut >= System.currentTimeMillis()) {
                byte bResult = waitForHDLCFrameStateMachine(iProtocolTimeout, rxFrame);
                if (bResult == HDLC_RX_OK) {
                    timeOut = System.currentTimeMillis() + iProtocolTimeout;    // Recalculate the timeout
                    HDLCFrame hdlcFrame = decodeFrame(rxFrame);
                    updateNS(hdlcFrame);
                    if (hdlcFrame.getbControl() == I) {
                        // Some checks removed that prevented the data from the A2 device to be read
                        if (hdlcFrame.getInformationBuffer() != null) {
                            try {
                                receiveBuffer.addArray(hdlcFrame.getInformationBuffer());
                            } catch (IOException e) {
                                throw new DLMSConnectionException(e);
                            }
                        }
                        NR = (byte) nextSequence(NR);
                        if ((hdlcFrame.getsFrameFormat() & HDLC_FRAME_S_BIT) != 0) {
                            if (hdlcFrame.isBoolControlPFBit()) {
                                startWriting();
                            }
                        } else {
                            return receiveBuffer.getArray();
                        }
                    } else if (hdlcFrame.getbControl() == RR) {
                        // received RR frame , expected I frame
                        if (hdlcFrame.isBoolControlPFBit()) {
                            if (NS == hdlcFrame.getNR()) {
                                startWriting();
                                sendFrame(txFrame); // resend last I frame
                            } else {
                                nrOfConsecutiveReceiveReadyCycles++;
                                startWriting();
                            }
                        }
                    } else {
                        // invalid frame type
                        throw new DLMSConnectionException("ERROR receiving data, should receive an I or RR frame.");
                    }
                } else {
                    startWriting();   //Retry
                    sendFrame(txFrame);
                }

                if (nrOfConsecutiveReceiveReadyCycles >= MAX_RECEIVE_INFORMATION_FIELD_CONSECUTIVE_RECEIVE_READY_CYCLES) {
                    throw new DLMSConnectionException("receiveInformationField> Maximum number of consecutive RR response cycles exceeded");
                }
            }
        }
        throw new IOException("receiveInformationField> Timeout and retry count exceeded when receiving data [" + iMaxRetries + " x " + iProtocolTimeout + " ms]");
    }

    public void setFrameBuilder(A2RequestFrameBuilder frameBuilder) {
        this.frameBuilder = frameBuilder;
    }

    public void createAssociation() {
        sendRequest(aarq);
    }
}

