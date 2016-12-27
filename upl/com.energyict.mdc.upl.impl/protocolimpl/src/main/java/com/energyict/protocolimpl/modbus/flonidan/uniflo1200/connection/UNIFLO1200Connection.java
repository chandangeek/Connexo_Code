package com.energyict.protocolimpl.modbus.flonidan.uniflo1200.connection;

import com.energyict.mdc.io.NestedIOException;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.modbus.core.ModbusException;
import com.energyict.protocolimpl.modbus.core.connection.ModbusConnection;
import com.energyict.protocolimpl.modbus.core.connection.RequestData;
import com.energyict.protocolimpl.modbus.core.connection.ResponseData;
import com.energyict.protocolimpl.modbus.core.functioncode.FunctionCode;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Copyrights EnergyICT
 * Date: 26-mei-2010
 * Time: 11:06:44
 */
public class UNIFLO1200Connection extends ModbusConnection {

    private static final int NO_DATA_AVAILABLE = -1;
    private static final int MAX_DELAY_ON_ERROR = 5000;
    private static final int DEFAULT_DELAY_BEFORE_FLUSH = 5;

    private Logger logger;

    /**
     * Creates a new instance of the ModbusConnection
     *
     * @param inputStream
     * @param outputStream
     * @param timeout
     * @param interframeTimeout
     * @param maxRetries
     * @param forcedDelay
     * @param echoCancelling
     * @param halfDuplexController
     * @throws ConnectionException
     */
    public UNIFLO1200Connection(InputStream inputStream, OutputStream outputStream, int timeout, int interframeTimeout, int maxRetries, long forcedDelay, int echoCancelling, HalfDuplexController halfDuplexController, Logger logger) throws ConnectionException {
        super(inputStream, outputStream, timeout, maxRetries, forcedDelay, echoCancelling, halfDuplexController, interframeTimeout, 0, 0);
        this.logger = logger;
        copyEchoBuffer();
        flushInputStream();
        flushEchoBuffer();
        flushInputStream();
    }

    /**
     * @param requestData
     * @return
     * @throws IOException
     */
    @Override
    public ResponseData sendRequest(RequestData requestData) throws IOException {
/*
        for (int i = 0; i < 50; i++) {
            RequestData rd = new RequestData(3);
            rd.setData(ProtocolTools.getBytesFromHexString("$00$6B$00$01"));
            assembleAndSend(rd);
            ProtocolTools.delay(10);
        }
*/

        int retries = getMaxRetries();
        do {
            flushInputStream();
            try {
                assembleAndSend(requestData);
                return receiveData(requestData);
            } catch (IOException e) {
                flushInputStream(getTimeout());
                retries--;
                getLogger().log(Level.WARNING, "Error while reading data: " + e.getMessage() + ", Retries: " + (getMaxRetries() - retries) + "/" + getMaxRetries());
            }
        } while (retries >= 0);
        throw new IOException("Unable to get response from device. Retries exceeded maximim number of retries: " + getMaxRetries());
    }

    /**
     * @param requestData
     * @return
     * @throws NestedIOException
     * @throws IOException
     */
    private ResponseData receiveData(RequestData requestData) throws IOException {
        ConnectionState cs = new ConnectionState();
        cs.setRequestData(requestData);
        cs.setProtocolTimeout(System.currentTimeMillis() + getTimeout());

        copyEchoBuffer();
        while (true) {
            cs.setKar(readIn());

            if (cs.getKar() != NO_DATA_AVAILABLE) {

                cs.getAllDataArrayOutputStream().write(cs.getKar());

                switch (cs.getState()) {
                    case ConnectionState.WAIT_FOR_ADDRESS:
                        waitForAddress(cs);
                        break;
                    case ConnectionState.WAIT_FOR_FUNCTIONCODE:
                        waitForFunctionCode(cs);
                        break;
                    case ConnectionState.WAIT_FOR_EXCEPTIONCODE:
                        return waitForExceptionCode(cs);
                    case ConnectionState.WAIT_FOR_LENGTH:
                        waitForLength(cs);
                        break;
                    case ConnectionState.WAIT_FOR_DATA:
                        if (waitForData(cs)) {
                            return cs.getResponseData();
                        }
                        break;
                    default:
                        throw new ProtocolConnectionException("receiveDataLength() invalid state!", PROTOCOL_ERROR);
                }
            } else {
                ProtocolTools.delay(1);
            }

            // in case of a response timeout
            if (System.currentTimeMillis() - cs.getProtocolTimeout() > 0) {
                throw new ProtocolConnectionException("receiveDataLength() response timeout error", TIMEOUT_ERROR);
            }

        }

    }

    @Override
    protected void flushInputStream() throws ConnectionException {
        flushInputStream(DEFAULT_DELAY_BEFORE_FLUSH);
    }

    private void flushInputStream(long delayIn) throws ConnectionException {
        long delay = delayIn > MAX_DELAY_ON_ERROR ? MAX_DELAY_ON_ERROR : delayIn;
        try {
            ProtocolTools.delay(delay);
            while (readIn() != NO_DATA_AVAILABLE) {
                super.flushInputStream();
                ProtocolTools.delay(getTimeout());
            }
        } catch (NestedIOException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param cs
     * @return
     * @throws ProtocolConnectionException
     */
    private boolean waitForData(ConnectionState cs) throws ProtocolConnectionException {
        cs.getResultDataArrayOutputStream().write(cs.getKar());
        cs.setLen(cs.getLen() - 1);
        if (cs.getLen() <= 0) {
            byte[] data = cs.getAllDataArrayOutputStream().toByteArray();
            if (data.length <= 2) {
                throw new ProtocolConnectionException("receiveDataLength() PROTOCOL Error", PROTOCOL_ERROR);
            }
            int crc = ((int) data[data.length - 1] & 0xff) << 8 | ((int) data[data.length - 2] & 0xff);
            data = ProtocolUtils.getSubArray2(data, 0, data.length - 2);
            int crc2 = CRCGenerator.calcCRCModbus(data);
            if (crc2 == crc) {
                data = cs.getResultDataArrayOutputStream().toByteArray();
                cs.getResponseData().setData(ProtocolUtils.getSubArray2(data, 0, data.length - 2));
                return true;
            } else {
                throw new ProtocolConnectionException("receiveDataLength() CRC Error", CRC_ERROR);
            }
        }
        return false;
    }

    /**
     * we should not use the length to check if the vcomplete frame is received. However, the problem lays within java not behaving
     * realtime enough to implement the correct Modbus Phy layer timing T = 3.5 kar
     * Gaps between receiving data from the underlaying serial logic can take up to 30 ms...
     *
     * @param cs
     */
    private void waitForLength(ConnectionState cs) throws IOException {
        cs.getResultDataArrayOutputStream().write(cs.getKar());
        int length = cs.getKar() + 2;
        if (length != cs.getRequestData().getRequestLength()) {
            throw new IOException("Asked for a length of " + cs.getRequestData().getRequestLength() + " but received packet with length " + length);
        }
        cs.setLen(length); // add 2 bytes for the CRC
        cs.setState(ConnectionState.WAIT_FOR_DATA);
    }

    /**
     * @param cs
     * @return
     * @throws ModbusException
     */
    private ResponseData waitForExceptionCode(ConnectionState cs) throws ModbusException {
        throw new ModbusException(cs.getFunctionErrorCode(), cs.getKar());
    }

    /**
     * @param cs
     * @throws ProtocolConnectionException
     */
    private void waitForFunctionCode(ConnectionState cs) throws ProtocolConnectionException {
        if (cs.getKar() == cs.getRequestData().getFunctionCode()) {
            cs.getResponseData().setFunctionCode(cs.getKar());
            if ((cs.getResponseData().getFunctionCode() == FunctionCode.WRITE_MULTIPLE_REGISTER.getFunctionCode()) ||
                    (cs.getResponseData().getFunctionCode() == FunctionCode.WRITE_SINGLE_REGISTER.getFunctionCode())) {
                cs.setLen(4 + 2);  // 4 bytes data + 2 bytes crc
                cs.setState(ConnectionState.WAIT_FOR_DATA);
            } else {
                cs.setState(ConnectionState.WAIT_FOR_LENGTH);
            }
        } else if (cs.getKar() == (cs.getRequestData().getFunctionCode() + 0x80)) {
            cs.setFunctionErrorCode(cs.getKar());
            cs.setState(ConnectionState.WAIT_FOR_EXCEPTIONCODE);
        } else {
            throw new ProtocolConnectionException("receiveDataLength() should receive the functioncode!", PROTOCOL_ERROR);
        }
    }

    /**
     * @param cs
     */
    private void waitForAddress(ConnectionState cs) throws IOException {
        if (cs.getKar() == getAddress()) {
            cs.getAllDataArrayOutputStream().reset();
            cs.getAllDataArrayOutputStream().write(cs.getKar());
            cs.getResponseData().setAddress(cs.getKar());
            cs.setState(ConnectionState.WAIT_FOR_FUNCTIONCODE);
            cs.setLen(0);
            cs.setProtocolTimeout(System.currentTimeMillis() + getTimeout());
        } else {
            cs.getAllDataArrayOutputStream().reset();
            throw new IOException("Invalid address: " + cs.getKar() + ", expected: " + getAddress());
        }
    }

    /**
     * Getter for the logger
     * @return
     */
    public Logger getLogger() {
        if (logger == null) {
            System.out.println("Logger was NULL");
            logger = Logger.getAnonymousLogger();
        }
        return logger;
    }

}
