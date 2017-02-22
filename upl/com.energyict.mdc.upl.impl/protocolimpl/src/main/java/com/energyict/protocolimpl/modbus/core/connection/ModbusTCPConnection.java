package com.energyict.protocolimpl.modbus.core.connection;

import com.energyict.cbo.NestedIOException;
import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.modbus.core.ModbusException;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Copyrights EnergyICT
 * User: sva
 * Date: 4/01/12
 * Time: 9:47
 */
public class ModbusTCPConnection extends ModbusConnection {

    /** unique identifier for each request/response pair (transaction pairing) **/
    int transactionIdentifier = 0;
    /** used for serial bridging, if a device is located on a non TCP/IP network - In normal cases this is not used. **/
    int unitIdentifier = 0;

    /**
     * Creates a new instance of the ModbusConnection
     *
     * @param inputStream
     * @param outputStream
     * @param timeout
     * @param maxRetries
     * @param forcedDelay
     * @param echoCancelling
     * @param halfDuplexController
     * @param interframeTimeout
     * @param responseTimeout
     * @param physicalLayer
     * @param unitIdentifier    the device slave address
     * @throws com.energyict.dialer.connection.ConnectionException
     *
     */
    public ModbusTCPConnection(InputStream inputStream, OutputStream outputStream, int timeout, int maxRetries, long forcedDelay, int echoCancelling, HalfDuplexController halfDuplexController, int interframeTimeout, int responseTimeout, int physicalLayer, int unitIdentifier) throws ConnectionException {
        super(inputStream, outputStream, timeout, maxRetries, forcedDelay, echoCancelling, halfDuplexController, interframeTimeout, responseTimeout, physicalLayer);
        this.unitIdentifier = unitIdentifier;
    }

    /**
     * Send a request and return the ResponseData
     *
     * @param requestData
     * @return
     * @throws java.io.IOException
     */
    @Override
    public ResponseData sendRequest(RequestData requestData) throws IOException {
        int retry = 0;
        while (true) {
            flushInputStream();
            ResponseData responseData;
            try {
                assembleAndSend(requestData);
                responseData = receiveData(requestData);
                if (responseData.isException()) {
                    throw new ProtocolConnectionException("ModbusConnection, sendRequest(), exception " + responseData.getExceptionString() + " received!");
                }
                return responseData;
            } catch (ConnectionException e) {
                if (retry++ >= getMaxRetries()) {
                    throw new ProtocolConnectionException("ModbusConnection, sendRequest(), error maxRetries (" + getMaxRetries() + "), " + e.getMessage(), MAX_RETRIES_ERROR);
                }
            } catch (ModbusException e) {
                if (retry++ >= getMaxRetries()) {
                    throw new ProtocolConnectionException("ModbusConnection, sendRequest(), error maxRetries (" + getMaxRetries() + "), " + e.getMessage(), MAX_RETRIES_ERROR);
                }
            } finally {
                flushOutputStream();
            }
        }
    }

    private ResponseData receiveData(RequestData requestData) throws ModbusException, NestedIOException, ConnectionException {

        byte[] responseDataFrame = null;
        long protocolTimeout = System.currentTimeMillis() + timeout;
        ModbusTCPHeader requestHeader = new ModbusTCPHeader(requestData, getTransactionIdentifier(), unitIdentifier);
        copyEchoBuffer();

        while ((responseDataFrame == null || responseDataFrame.length < 8)) {
            if (responseDataFrame != null) {
                responseDataFrame = ProtocolTools.concatByteArrays(responseDataFrame, readInArray());
            } else {
                responseDataFrame = readInArray();
            }

            if (((long) (System.currentTimeMillis() - protocolTimeout)) > 0) {
                throw new ProtocolConnectionException("receiveDataModbus(): receiveDataLength() response timeout error", TIMEOUT_ERROR);
            }
        }

        // Parse the received response
        ModbusTCPHeader responseHeader = new ModbusTCPHeader(ProtocolTools.getSubArray(responseDataFrame, 0, 7));
        int responseFunctionCode = ProtocolTools.getIntFromBytes(responseDataFrame, 7, 1);

        if (responseHeader.getTransactionIdentifier() != requestHeader.getTransactionIdentifier()) {
            throw new ModbusException("receiveDataModbus(): Transaction Identifier from the response (" + responseHeader.getTransactionIdentifier() +
                ") is different from the Transaction Identifier of the request (" + requestHeader.getTransactionIdentifier() + ").");

        } else if (responseHeader.getProtocolIdentifier() != requestHeader.getProtocolIdentifier()) {
            throw new ModbusException("receiveDataModbus(): Protocol Identifier from the response (" + responseHeader.getProtocolIdentifier() +
                ") is different from the Protocol Identifier of the request (" + requestHeader.getProtocolIdentifier() + ").");

        } else if (responseHeader.getUnitIdentifier() != requestHeader.getUnitIdentifier()) {
            throw new ModbusException("receiveDataModbus(): Unit Identifier from the response (" + responseHeader.getUnitIdentifier() +
                ") is different from the Unit Identifier of the request (" + requestHeader.getUnitIdentifier() + ").");

        } else if (responseHeader.getLengthField() != (ProtocolTools.getSubArray(responseDataFrame, 6).length)) {
            throw new ModbusException("receiveDataModbus(): The length of the received response ("+(ProtocolTools.getSubArray(responseDataFrame, 6).length)+
                    ") is different from the expected length (" + responseHeader.getLengthField() + ").");

        } else if (responseFunctionCode != requestData.getFunctionCode()) {
            if (responseFunctionCode == (requestData.getFunctionCode() + 0x80)) {
                int errorCode = ProtocolTools.getIntFromBytes(responseDataFrame, 8, 1);
                throw new ModbusException(responseFunctionCode, errorCode);
            } else {
                throw new ModbusException("receiveDataModbus(): Function code from the response (" + responseFunctionCode +
                        ") is different from the function code of the request (" + requestData.getFunctionCode() + ").");
            }
        }

        ResponseData responseData = new ResponseData();
        responseData.setFunctionCode(responseFunctionCode);
        responseData.setData(ProtocolTools.getSubArray(responseDataFrame, 8));
        return responseData;
    }

    /**
     * Assemble a valid request and send it to the device
     *
     * @param requestData
     * @throws com.energyict.cbo.NestedIOException
     *
     * @throws com.energyict.dialer.connection.ConnectionException
     *
     */
    @Override
    protected void assembleAndSend(RequestData requestData) throws ConnectionException {
        try {
            ModbusTCPHeader header = new ModbusTCPHeader(requestData, getNewTransactionIdentifier(), unitIdentifier);
            byte[] data = ProtocolUtils.concatByteArrays(header.getHeaderBytes(), requestData.getFrameData());
            sendRawData(data);
        } catch (NestedIOException e) {
            throw new ProtocolConnectionException(e.getCause().getMessage(), PROTOCOL_ERROR);
        }
    }

    public int getNewTransactionIdentifier() {
        transactionIdentifier += 1;
        return transactionIdentifier;
    }

    public int getTransactionIdentifier() {
        return transactionIdentifier;
    }

    @Override
    public MeterType connectMAC(String strID, String strPassword, int securityLevel, String nodeId) throws IOException, ProtocolConnectionException {
        //do nothing
        return null;
    }
}