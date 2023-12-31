package com.energyict.protocolimpl.edmi.common.connection;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.edmi.common.core.ResponseData;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author cisac
 */
public class MiniECommandLineConnection extends ExtendedCommandLineConnection implements CommandLineConnection {

    byte[] miniECommandHeader = new byte[1];

    /**
     * Creates a new instance of AlphaConnection
     */
    public MiniECommandLineConnection(InputStream inputStream,
                                      OutputStream outputStream,
                                      int timeout,
                                      int maxRetries,
                                      long forcedDelay,
                                      int echoCancelling,
                                      HalfDuplexController halfDuplexController,
                                      String serialNumber) {

        super(inputStream, outputStream, timeout, maxRetries, forcedDelay, echoCancelling, halfDuplexController, serialNumber);
    }

    @Override
    protected void genSequenceNr() {
        if ((sequenceNr == 0) || (sequenceNr >= 0xF)) {
            sequenceNr = 1;
        } else {
            sequenceNr++;
        }
    }

    private byte[] generateMiniECommandHeader() {
        int firstSibling = 8 << 4;
        genSequenceNr();
        int completeHeader = firstSibling | sequenceNr;
        return ProtocolTools.getBytesFromInt(completeHeader, 1);
    }

    @Override
    protected void prepareAndAssembleTxFrame(byte[] rawData) {
        txOutputStream.reset();
        byte[] cmdData = rawData;
        if (isExtendedCommunication()) {
            setMiniECommandHeader(generateMiniECommandHeader());// initialize miniEHeader. It is used later for comparison
            cmdData = ProtocolUtils.concatByteArrays(getMiniECommandHeader(), rawData);
        }
        if ((cmdData != null) && (cmdData.length > 0)) {
            for (byte aCmdData : cmdData) {
                assembleFrame(aCmdData);
            }
        }
    }

    private byte[] getMiniECommandHeader() {
        return miniECommandHeader;
    }

    private void setMiniECommandHeader(byte[] header) {
        this.miniECommandHeader = header;
    }

    @Override
    public ResponseData receiveFrame() throws ConnectionException {

        long protocolTimeout, interFrameTimeout;
        int kar;
        ByteArrayOutputStream resultArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream allDataArrayOutputStream = new ByteArrayOutputStream();

        interFrameTimeout = System.currentTimeMillis() + timeout;
        protocolTimeout = System.currentTimeMillis() + TIMEOUT;

        resultArrayOutputStream.reset();
        allDataArrayOutputStream.reset();

        copyEchoBuffer();
        while (true) {
            while ((kar = readIn()) != -1) {
                delay(10);
                allDataArrayOutputStream.write(kar);
                interFrameTimeout = System.currentTimeMillis() + timeout;
            }

            if (allDataArrayOutputStream.size() > 0) {
                // Calc CRC
                byte[] rxFrame = allDataArrayOutputStream.toByteArray();
                if (getMiniECommandHeader()[0] != rxFrame[0]) {
                    throw new ProtocolConnectionException(
                            "Received Mini-E header '"
                                    + ProtocolTools.getHexStringFromBytes(new byte[]{rxFrame[0]}, "")
                                    + "' does not have the expected value: '"
                                    + ProtocolTools.getHexStringFromBytes(getMiniECommandHeader())
                                    + "'");
                }

                if (CRCGenerator.ccittCRC(rxFrame) == 0) {
                    return new ResponseData(ProtocolUtils.getSubArray(rxFrame, 1, rxFrame.length - 3)); //remove header and last 2 CRC bytes
                } else {
                    // ERROR, CRC error
                    throw new ProtocolConnectionException("receiveFrame() response crc error", CRC_ERROR);
                }
            }

            if (((System.currentTimeMillis() - protocolTimeout)) > 0) {
                throw new ProtocolConnectionException("receiveFrame() response timeout error", TIMEOUT_ERROR);
            }
            if (((System.currentTimeMillis() - interFrameTimeout)) > 0) {
                throw new ProtocolConnectionException("receiveFrame() interframe timeout error", TIMEOUT_ERROR);
            }
        }
    }
}