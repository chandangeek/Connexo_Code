/*
 * SDKSampleProtocolConnection.java
 *
 * Created on 13 juni 2007, 11:42
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ametek;

import com.energyict.mdc.io.NestedIOException;

import com.energyict.dialer.connection.Connection;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.base.Encryptor;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author kvds
 */
public class JemProtocolConnection extends Connection implements ProtocolConnection {

    private static byte[] ack = {0x10, 0x01};
    private static byte[] cmdStart = {0x10, 0x02};
    private static byte[] cmdEnd = {0x10, 0x03};

    InputStream inputStream;
    OutputStream outputStream;
    int iTimeout;
    int iMaxRetries;
    long lForceDelay;
    int iEchoCancelling;
    int iIEC1107Compatible;
    Encryptor encryptor;
    int iProtocolTimeout;
    boolean boolFlagIEC1107Connected;
    Logger logger;

    /**
     * Creates a new instance of SDKSampleProtocolConnection
     */
    public JemProtocolConnection(InputStream inputStream,
                                 OutputStream outputStream,
                                 int iTimeout,
                                 int iMaxRetries,
                                 long lForceDelay,
                                 int iEchoCancelling,
                                 int iIEC1107Compatible,
                                 Encryptor encryptor, Logger logger) throws ConnectionException {
        super(inputStream, outputStream, lForceDelay, iEchoCancelling);
        this.iMaxRetries = iMaxRetries;
        this.lForceDelay = lForceDelay;
        this.iIEC1107Compatible = iIEC1107Compatible;
        this.encryptor = encryptor;
        iProtocolTimeout = iTimeout;
        boolFlagIEC1107Connected = false;
        this.logger = logger;
        this.outputStream = outputStream;
    }

    public void setHHUSignOn(HHUSignOn hhuSignOn) {

    }

    public HHUSignOn getHhuSignOn() {
        return null;
    }

    public void disconnectMAC() throws NestedIOException, ProtocolConnectionException {
        logger.info("call connection class disconnectMAC(...)");
    }

    public MeterType connectMAC(String strID, String strPassword, int securityLevel, String nodeId) throws IOException, ProtocolConnectionException {
        logger.info("call connection class connectMAC(...)");
        return null;
    }

    public byte[] dataReadout(String strID, String nodeId) throws NestedIOException, ProtocolConnectionException {
        return null;
    }

    public void delayAndFlush(int delay) throws IOException {
        super.delayAndFlush(delay);
    }

    public void sendRequest(byte[] data) throws IOException {
        byte[] checkSumBytes = getCheckSumBytes(data, data.length);

        outputStream.write(ack);
        outputStream.write(data);
        outputStream.write(checkSumBytes);
    }

    public byte[] sendRequestAndReceiveResponse(byte[] data) throws IOException {
        byte[] checkSumBytes = getCheckSumBytes(data, data.length);

        outputStream.write(ack);
        outputStream.write(data);
        outputStream.write(checkSumBytes);
        return receiveResponse();
    }

    public byte[] receiveResponse() throws IOException {
        return receiveResponse(-1);
    }

    private final int WAIT_FOR_START = 0;
    private final int WAIT_FOR_START2 = 1;
    private final int WAIT_FOR_DATA = 2;
    private final int CHECK_FOR_DOUBLE_DLE = 3;
    private final int WAIT_FOR_CRC = 4;
    private final int DEBUG = 0;

    public byte[] receiveResponse(long timeoutEnq) throws IOException {
        long interFrameTimeout;
        int kar;
        int count = 0;
        int state = WAIT_FOR_START;
        ByteArrayOutputStream resultArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream inStream = new ByteArrayOutputStream();

        interFrameTimeout = System.currentTimeMillis() + (timeoutEnq == -1 ? iProtocolTimeout : timeoutEnq);

        if (DEBUG >= 1) {
            System.out.println("KV_DEBUG> timeout=" + iProtocolTimeout);
        }

        resultArrayOutputStream.reset();
        copyEchoBuffer();
        boolean doneReading = false;
        while (true) {

            if ((kar = readIn()) != -1) {

                if (DEBUG >= 2) {
                    System.out.print(",0x");
                    ProtocolUtils.outputHex(((int) kar));
                }
                switch (state) {

                    case WAIT_FOR_START: {
                        if (kar == DLE) {
                            count = 1;
                            state = WAIT_FOR_START2;
                        } else if (kar == ACK) {
                            resultArrayOutputStream.write(kar);
                            return resultArrayOutputStream.toByteArray();
                        } else {
                            throw new ProtocolConnectionException("receiveResponse() 0x" + Integer.toHexString(kar) + " received instead of DLE at beginning of response.", PROTOCOL_ERROR);
                        }

                    }
                    break; // WAIT_FOR_START

                    case WAIT_FOR_START2: {
                        count++;
                        //DLE(1) SOH(1) ADR(1) CMD(1) EXT(1) DLE(1) STX(1) = 7
                        if (count > 2) {
                            inStream.write(kar);
                        }
                        if (count == 7) {
                            if (kar == STX) { //Start of Text
                                state = WAIT_FOR_DATA;
                                count = 0;
                            } else {
                                throw new ProtocolConnectionException("receiveResponse() Error processing header", PROTOCOL_ERROR);
                            }
                        }
                    }
                    break; // WAIT_FOR_START2

                    case WAIT_FOR_DATA: {
                        if (kar != DLE) {
                            inStream.write(kar);
                            resultArrayOutputStream.write(kar);
                        } else {
                            state = CHECK_FOR_DOUBLE_DLE;
                            inStream.write(kar);
                        }
                    }
                    break; // WAIT_FOR_DATA

                    case CHECK_FOR_DOUBLE_DLE: {
                        if (kar == DLE) {
                            inStream.write(kar);
                            resultArrayOutputStream.write(kar);
                            state = WAIT_FOR_DATA;
                        } else if (kar == ETB) {
                            inStream.write(kar);
                            state = WAIT_FOR_CRC;
                        } else if (kar == ETX) {
                            inStream.write(kar);
                            doneReading = true;
                            state = WAIT_FOR_CRC;
                        }
                    }
                    break; // WAIT_FOR_DATA

                    case WAIT_FOR_CRC: {
                        inStream.write(kar);
                        count++;
                        if (count == 2) {
                            byte[] data = inStream.toByteArray();
                            if (verifyCheck(data)) {
                                if (doneReading) {
                                    return resultArrayOutputStream.toByteArray();
                                } else {
                                    inStream = new ByteArrayOutputStream();
                                    count = 0;
                                    state = WAIT_FOR_START;
                                    interFrameTimeout = System.currentTimeMillis() + (timeoutEnq == -1 ? iProtocolTimeout : timeoutEnq);
                                    outputStream.write(ACK);
                                }
                            } else {
                                throw new ProtocolConnectionException("receiveResponse() crc error", CRC_ERROR);
                            }
                        } // if (--count <= 0)
                    } // WAIT_FOR_CRC
                } // switch(state)
            } // if ((kar = readIn()) != -1)

            if (((long) (System.currentTimeMillis() - interFrameTimeout)) > 0) {
                throw new ProtocolConnectionException("receiveResponse() interframe timeout error", TIMEOUT_ERROR);
            }
        } // while(true)
    } // public Response receiveResponse()

    protected boolean verifyCheck(byte[] in) {
        byte[] check = getCheckSumBytes(in, in.length - 2);
        byte b1 = in[in.length - 2];
        byte b2 = in[in.length - 1];

        if (b1 == check[0] && b2 == check[1]) {
            return true;
        } else {
            return false;
        }
    }

    protected byte[] getCheckSumBytes(byte[] send, int length) {
        List sendList = new ArrayList();
        //from 3 b\c we skip DLE(1) and SOH(1) and ADR(1)
        for (int i = 0; i < length; i++) {
            if (send[i] != 0x10) {
                sendList.add(new Byte(send[i]));
            } else if (i < (length + 1) && send[i] == 0x10 && send[i + 1] == 0x10) {
                i++;
                sendList.add(new Byte(send[i]));
            }
        }

        Byte[] crcArr = new Byte[sendList.size()];
        sendList.toArray(crcArr);
        int c1 = j10crc(crcArr, crcArr.length);

        String ss = Integer.toHexString(c1);
        while (ss.length() < 4) {
            ss = "0" + ss;
        }

        int b1 = Integer.parseInt(ss.substring(0, 2), 16);
        int b2 = Integer.parseInt(ss.substring(2, 4), 16);

        return new byte[]{(byte) b2, (byte) b1};
    }

    protected int j10crc(Byte[] txbuf, long buflen) {
        int crc;
        int i;

        for (i = 0, crc = 0; i < buflen; i++) {
            crc = CRC16_1[(txbuf[i].byteValue() ^ crc) & 0xFF] ^ (crc >> 8);
        }
        return (crc);
    }

    public static byte[] getCmdStart() {
        return cmdStart;
    }

    public static byte[] getCmdEnd() {
        return cmdEnd;
    }

    static public final int[] CRC16_1 = {
            0x0000, 0xC0C1, 0xC181, 0x0140, 0xC301, 0x03C0, 0x0280, 0xC241,
            0xC601, 0x06C0, 0x0780, 0xC741, 0x0500, 0xC5C1, 0xC481, 0x0440,
            0xCC01, 0x0CC0, 0x0D80, 0xCD41, 0x0F00, 0xCFC1, 0xCE81, 0x0E40,
            0x0A00, 0xCAC1, 0xCB81, 0x0B40, 0xC901, 0x09C0, 0x0880, 0xC841,
            0xD801, 0x18C0, 0x1980, 0xD941, 0x1B00, 0xDBC1, 0xDA81, 0x1A40,
            0x1E00, 0xDEC1, 0xDF81, 0x1F40, 0xDD01, 0x1DC0, 0x1C80, 0xDC41,
            0x1400, 0xD4C1, 0xD581, 0x1540, 0xD701, 0x17C0, 0x1680, 0xD641,
            0xD201, 0x12C0, 0x1380, 0xD341, 0x1100, 0xD1C1, 0xD081, 0x1040,
            0xF001, 0x30C0, 0x3180, 0xF141, 0x3300, 0xF3C1, 0xF281, 0x3240,
            0x3600, 0xF6C1, 0xF781, 0x3740, 0xF501, 0x35C0, 0x3480, 0xF441,
            0x3C00, 0xFCC1, 0xFD81, 0x3D40, 0xFF01, 0x3FC0, 0x3E80, 0xFE41,
            0xFA01, 0x3AC0, 0x3B80, 0xFB41, 0x3900, 0xF9C1, 0xF881, 0x3840,
            0x2800, 0xE8C1, 0xE981, 0x2940, 0xEB01, 0x2BC0, 0x2A80, 0xEA41,
            0xEE01, 0x2EC0, 0x2F80, 0xEF41, 0x2D00, 0xEDC1, 0xEC81, 0x2C40,
            0xE401, 0x24C0, 0x2580, 0xE541, 0x2700, 0xE7C1, 0xE681, 0x2640,
            0x2200, 0xE2C1, 0xE381, 0x2340, 0xE101, 0x21C0, 0x2080, 0xE041,
            0xA001, 0x60C0, 0x6180, 0xA141, 0x6300, 0xA3C1, 0xA281, 0x6240,
            0x6600, 0xA6C1, 0xA781, 0x6740, 0xA501, 0x65C0, 0x6480, 0xA441,
            0x6C00, 0xACC1, 0xAD81, 0x6D40, 0xAF01, 0x6FC0, 0x6E80, 0xAE41,
            0xAA01, 0x6AC0, 0x6B80, 0xAB41, 0x6900, 0xA9C1, 0xA881, 0x6840,
            0x7800, 0xB8C1, 0xB981, 0x7940, 0xBB01, 0x7BC0, 0x7A80, 0xBA41,
            0xBE01, 0x7EC0, 0x7F80, 0xBF41, 0x7D00, 0xBDC1, 0xBC81, 0x7C40,
            0xB401, 0x74C0, 0x7580, 0xB541, 0x7700, 0xB7C1, 0xB681, 0x7640,
            0x7200, 0xB2C1, 0xB381, 0x7340, 0xB101, 0x71C0, 0x7080, 0xB041,
            0x5000, 0x90C1, 0x9181, 0x5140, 0x9301, 0x53C0, 0x5280, 0x9241,
            0x9601, 0x56C0, 0x5780, 0x9741, 0x5500, 0x95C1, 0x9481, 0x5440,
            0x9C01, 0x5CC0, 0x5D80, 0x9D41, 0x5F00, 0x9FC1, 0x9E81, 0x5E40,
            0x5A00, 0x9AC1, 0x9B81, 0x5B40, 0x9901, 0x59C0, 0x5880, 0x9841,
            0x8801, 0x48C0, 0x4980, 0x8941, 0x4B00, 0x8BC1, 0x8A81, 0x4A40,
            0x4E00, 0x8EC1, 0x8F81, 0x4F40, 0x8D01, 0x4DC0, 0x4C80, 0x8C41,
            0x4400, 0x84C1, 0x8581, 0x4540, 0x8701, 0x47C0, 0x4680, 0x8641,
            0x8201, 0x42C0, 0x4380, 0x8341, 0x4100, 0x81C1, 0x8081, 0x4040,
    };
}
