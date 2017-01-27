/*
 * ModbusConnection.java
 *
 * Created on 19 september 2005, 16:01
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.core.connection;

import com.energyict.dialer.connection.ConnectionException;
import com.energyict.dialer.connection.ConnectionRS485;
import com.energyict.dialer.connection.HHUSignOn;
import com.energyict.dialer.core.HalfDuplexController;
import com.energyict.mdc.io.NestedIOException;
import com.energyict.protocol.exceptions.ConnectionCommunicationException;
import com.energyict.protocol.meteridentification.MeterType;
import com.energyict.protocolimpl.base.CRCGenerator;
import com.energyict.protocolimpl.base.ProtocolConnection;
import com.energyict.protocolimpl.base.ProtocolConnectionException;
import com.energyict.protocolimpl.modbus.core.ModbusException;
import com.energyict.protocolimpl.modbus.core.functioncode.FunctionCode;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author Koen
 */
public class ModbusConnection extends ConnectionRS485 implements ProtocolConnection {

    private static final int DEBUG = 0;

    int timeout;
    int maxRetries;

    int address;
    int interframeTimeout;
    int responseTimeout;
    int physicalLayer;

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
     * @throws ConnectionException
     */
    public ModbusConnection(InputStream inputStream,
                            OutputStream outputStream,
                            int timeout,
                            int maxRetries,
                            long forcedDelay,
                            int echoCancelling,
                            HalfDuplexController halfDuplexController,
                            int interframeTimeout,
                            int responseTimeout,
                            int physicalLayer) throws ConnectionException {
        super(inputStream, outputStream, forcedDelay, echoCancelling, halfDuplexController);
        this.timeout = timeout;
        this.maxRetries = maxRetries;
        this.interframeTimeout = interframeTimeout;
        this.responseTimeout = responseTimeout;
        this.physicalLayer = physicalLayer;

    } // ModbusConnection(...)

    /**
     * Setter for the address property
     *
     * @param address
     */
    public void setAddress(int address) {
        this.address = address;
    }

    /**
     * Getter for the address property
     *
     * @return
     */
    public int getAddress() {
        return address;
    }

    /**
     * Assemble a valid request and send it to the device
     *
     * @param requestData
     * @throws NestedIOException
     * @throws ConnectionException
     */
    protected void assembleAndSend(RequestData requestData) throws ConnectionException {
        try {
            byte[] data = ProtocolUtils.concatByteArrays(new byte[]{(byte) getAddress()}, requestData.getFrameData());
            int crc = CRCGenerator.calcCRCModbus(data);
            sendRawData(ProtocolUtils.concatByteArrays(data, new byte[]{(byte) (crc % 256), (byte) (crc / 256)}));
        } catch (NestedIOException e) {
            throw new ProtocolConnectionException(e.getCause().getMessage());
        }
    }

    /**
     * Send a request and return the ResponseData
     *
     * @param requestData
     * @return
     * @throws IOException
     */
    public ResponseData sendRequest(RequestData requestData) throws IOException {
        int retry = 0;
        while (true) {
            flushInputStream();
            ResponseData responseData;
            try {
                assembleAndSend(requestData);
                if (requestData.getFunctionCode() == 0x2B) { // in case of read device identification, always use modbus phy because there is no length in the frame
                    responseData = receiveDataModbus(requestData);
                } else if (physicalLayer == 0) {
                    responseData = receiveDataLength(requestData); // use datalength
                } else if (physicalLayer == 1) {
                    responseData = receiveDataModbus(requestData); // following modbus specs
                } else {
                    throw new ProtocolConnectionException("ModbusConnection, sendRequest(), unknow physicalLayer=" + physicalLayer + " property! Correct first");
                }

                return responseData;
            } catch (ConnectionException e) { // A connection related timeout exception
                if (retry++ >= getMaxRetries()) {
                    throw e;
                }
            } catch (ModbusException e) { // A non-connection modbus specific related exception > in some cases reties are applicable
                if (!e.isRetryAllowed()) {  // In case retrying is useless (we will hit the same error again), immediately throw the error
                    throw e;
                }
                if (retry++ >= getMaxRetries()) {
                    throw e;
                }
            }
        }
    }

    static private final int STATE_WAIT_FOR_ADDRESS = 0;
    static private final int STATE_WAIT_FOR_FUNCTIONCODE = 1;
    static private final int STATE_WAIT_FOR_DATA = 2;
    static private final int STATE_WAIT_FOR_LENGTH = 3;
    static private final int STATE_WAIT_FOR_EXCEPTIONCODE = 4;

    /**
     * This method sends a request to the device, and waits for a response using the length field in the received packet.
     * Only used when physicalLayer == 0
     *
     * @param requestData
     * @return
     * @throws IOException
     */
    protected ResponseData receiveDataLength(RequestData requestData) throws ModbusException, ConnectionException {
        long protocolTimeout;
        int kar;
        int state = STATE_WAIT_FOR_ADDRESS;
        ResponseData responseData = new ResponseData();
        ByteArrayOutputStream resultDataArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream allDataArrayOutputStream = new ByteArrayOutputStream();

        protocolTimeout = System.currentTimeMillis() + timeout;
        int len = 0;
        int functionErrorCode = 0;
        resultDataArrayOutputStream.reset();
        allDataArrayOutputStream.reset();
        if (DEBUG >= 2) {
            System.out.println("receiveDataLength(...):");
        }
        copyEchoBuffer();
        while (true) {

            try {
                if ((kar = readIn()) != -1) {

                    if (DEBUG >= 2) {
                        System.out.print(",0x");
                        ProtocolUtils.outputHex(kar);
                    }

                    allDataArrayOutputStream.write(kar); // accumulate frame

                    switch (state) {
                        case STATE_WAIT_FOR_ADDRESS: {
                            if (kar == getAddress()) {
                                allDataArrayOutputStream.reset();
                                allDataArrayOutputStream.write(kar);
                                responseData.setAddress(kar);
                                state = STATE_WAIT_FOR_FUNCTIONCODE;
                                len = 0;
                                protocolTimeout = System.currentTimeMillis() + responseTimeout;
                            } else {
                                allDataArrayOutputStream.reset();
                            }
                        }
                        break; // STATE_WAIT_FOR_ADDRESS

                        case STATE_WAIT_FOR_FUNCTIONCODE: {
                            if (kar == requestData.getFunctionCode()) {
                                responseData.setFunctionCode(kar);

                                if ((responseData.getFunctionCode() == FunctionCode.WRITE_MULTIPLE_REGISTER.getFunctionCode()) ||
                                        (responseData.getFunctionCode() == FunctionCode.WRITE_SINGLE_REGISTER.getFunctionCode()) ||
                                        (responseData.getFunctionCode() == FunctionCode.WRITE_SINGLE_COIL.getFunctionCode()) ||
                                        (responseData.getFunctionCode() == FunctionCode.WRITE_MULTIPLE_COILS.getFunctionCode())) {
                                    len = 4 + 2;  // 4 bytes data + 2 bytes crc
                                    state = STATE_WAIT_FOR_DATA;
                                } else {
                                    state = STATE_WAIT_FOR_LENGTH;
                                }
                            } else if (kar == (requestData.getFunctionCode() + 0x80)) {
                                functionErrorCode = kar;
                                state = STATE_WAIT_FOR_EXCEPTIONCODE;
                            } else {
                                throw new ModbusException("receiveDataLength() should receive the functioncode!");
                            }
                        }
                        break; // STATE_WAIT_FOR_FUNCTIONCODE

                        case STATE_WAIT_FOR_EXCEPTIONCODE: {
                            throw new ModbusException(functionErrorCode, kar);
                        } // STATE_WAIT_FOR_EXCEPTIONCODE

                        case STATE_WAIT_FOR_LENGTH: {
                            resultDataArrayOutputStream.write(kar);
                            len = (kar + 2); // add 2 bytes for the CRC
                            state = STATE_WAIT_FOR_DATA;
                        }
                        break; // STATE_WAIT_FOR_LENGTH

                        // we should not use the length to check if the complete frame is received. However, the problem lays within java not behaving
                        // realtime enough to implement the correct Modbus Phy layer timing T = 3.5 kar
                        // Gaps between receiving data from the underlying serial logic can take up to 30 ms...
                        case STATE_WAIT_FOR_DATA: {
                            resultDataArrayOutputStream.write(kar);
                            if (--len <= 0) {
                                try {
                                    Thread.sleep(interframeTimeout);
                                }
                                catch (InterruptedException e) {
                                    Thread.currentThread().interrupt();
                                    throw ConnectionCommunicationException.communicationInterruptedException(e);
                                }
                                byte[] data = allDataArrayOutputStream.toByteArray();
                                if (data.length <= 2) {
                                    throw new ModbusException("receiveDataLength() PROTOCOL Error");
                                }
                                int crc = ((int) data[data.length - 1] & 0xff) << 8 | ((int) data[data.length - 2] & 0xff);
                                data = ProtocolUtils.getSubArray2(data, 0, data.length - 2);
                                int crc2 = CRCGenerator.calcCRCModbus(data);
                                if (crc2 == crc) {
                                    data = resultDataArrayOutputStream.toByteArray();
                                    responseData.setData(ProtocolUtils.getSubArray2(data, 0, data.length - 2));
                                    if (DEBUG >= 2) {
                                        System.out.println("KV_DEBUG> " + responseData);
                                    }
                                    return responseData;
                                } else {
                                    if (DEBUG >= 2) {
                                        System.out.println("KV_DEBUG> CRC_ERROR ");
                                    }
                                    throw new ModbusException("receiveDataLength() CRC Error");
                                }
                            }

                        }
                        break; // STATE_WAIT_FOR_DATA

                        default:
                            throw new ModbusException("receiveDataLength() invalid state!");

                    } // switch(iState)

                    //allDataArrayOutputStream.write(kar); // accumulate frame

                } // if ((iNewKar = readIn()) != -1)
            } catch (NestedIOException e) {     // wrapped around InterruptedException
                throw new ProtocolConnectionException(e.getCause().getMessage());
            }

            // in case of a response timeout
            if (System.currentTimeMillis() - protocolTimeout > 0) {
                throw new ProtocolConnectionException("receiveDataLength() response timeout error", TIMEOUT_ERROR);
            }

        } // while(true)

    } // private ResponseData receiveData(RequestData requestData) throws NestedIOException, IOException


    /**
     * This method sends a request to the device, and waits for a response using the ModBus timing specifications
     * Only used when physicalLayer == 1
     *
     * @param requestData
     * @return
     * @throws NestedIOException
     * @throws IOException
     */
    protected ResponseData receiveDataModbus(RequestData requestData) throws ConnectionException, ModbusException {
        long protocolTimeout, interframe;
        int kar;
        int state = STATE_WAIT_FOR_ADDRESS;
        int functionErrorCode = 0;
        ResponseData responseData = new ResponseData();
        ByteArrayOutputStream resultDataArrayOutputStream = new ByteArrayOutputStream();
        ByteArrayOutputStream allDataArrayOutputStream = new ByteArrayOutputStream();

        protocolTimeout = System.currentTimeMillis() + timeout;
        // We should implement an phy abstraction layer with manageable parameters. Because we fix the interframe timeout at the
        // 2400 (lowest?) baudrate, performance when reading lots of modbusmeters real-time can degrade!
        interframe = System.currentTimeMillis() + timeout;

        resultDataArrayOutputStream.reset();
        allDataArrayOutputStream.reset();
        if (DEBUG >= 2) {
            System.out.println("receiveData(...):");
        }
        copyEchoBuffer();
        while (true) {
            try {
                if ((kar = readIn()) != -1) {

                    if (state != STATE_WAIT_FOR_ADDRESS) {
                        interframe = System.currentTimeMillis() + interframeTimeout;
                    } // // 3.5 cher T @ 2400 (supposed as lowest baudrate)

                    if (DEBUG >= 2) {
                        System.out.print(",0x");
                        ProtocolUtils.outputHex(kar);
                    }

                    allDataArrayOutputStream.write(kar); // accumulate frame

                    switch (state) {
                        case STATE_WAIT_FOR_ADDRESS: {
                            if (kar == getAddress()) {
                                allDataArrayOutputStream.reset();
                                allDataArrayOutputStream.write(kar);
                                responseData.setAddress(kar);
                                state = STATE_WAIT_FOR_FUNCTIONCODE;
                                if (DEBUG >= 2) {
                                    System.out.println("KV_DEBUG> address received");
                                }
                            } else {
                                allDataArrayOutputStream.reset();
                            }
                        }
                        break; // STATE_WAIT_FOR_ADDRESS

                        case STATE_WAIT_FOR_FUNCTIONCODE: {
                            if (kar == requestData.getFunctionCode()) {
                                responseData.setFunctionCode(kar);
                                state = STATE_WAIT_FOR_DATA;
                            } else if (kar == (requestData.getFunctionCode() + 0x80)) {
                                functionErrorCode = kar;
                                state = STATE_WAIT_FOR_EXCEPTIONCODE;
                            } else {
                                throw new ProtocolConnectionException("receiveDataModbus() should receive the functioncode!", PROTOCOL_ERROR);
                            }

                        }
                        break; // STATE_WAIT_FOR_FUNCTIONCODE

                        case STATE_WAIT_FOR_EXCEPTIONCODE: {
                            throw new ModbusException(functionErrorCode, kar);
                        } // STATE_WAIT_FOR_EXCEPTIONCODE

                        case STATE_WAIT_FOR_DATA: {
                            resultDataArrayOutputStream.write(kar);
                        }
                        break; // STATE_WAIT_FOR_DATA

                        default:
                            throw new ProtocolConnectionException("receiveDataModbus() invalid state!", PROTOCOL_ERROR);

                    } // switch(iState)

                    //allDataArrayOutputStream.write(kar); // accumulate frame

                } // if ((iNewKar = readIn()) != -1)
            } catch (NestedIOException e) {
                throw new ProtocolConnectionException(e.getCause().getMessage());
            }

            // frame received, check validity
            if (state != STATE_WAIT_FOR_ADDRESS) {
                if (System.currentTimeMillis() - interframe > 0) {
                    byte[] data = allDataArrayOutputStream.toByteArray();
                    if (data.length <= 2) {
                        throw new ProtocolConnectionException("receiveDataModbus() PROTOCOL Error", PROTOCOL_ERROR);
                    }
                    int crc = ((int) data[data.length - 1] & 0xff) << 8 | ((int) data[data.length - 2] & 0xff);
                    data = ProtocolUtils.getSubArray2(data, 0, data.length - 2);
                    int crc2 = CRCGenerator.calcCRCModbus(data);
                    if (crc2 == crc) {
                        data = resultDataArrayOutputStream.toByteArray();
                        responseData.setData(ProtocolUtils.getSubArray2(data, 0, data.length - 2));
                        if (DEBUG >= 2) {
                            System.out.println("KV_DEBUG> " + responseData);
                        }
                        return responseData;
                    } else {
                        if (DEBUG >= 2) {
                            System.out.println("KV_DEBUG> CRC_ERROR ");
                        }
                        throw new ProtocolConnectionException("receiveDataModbus() CRC Error", CRC_ERROR);
                    }
                } // if (((long) (System.currentTimeMillis() - interFrameTimeout)) > 0)
            } // if (state != STATE_WAIT_FOR_ADDRESS)

            // in case of a response timeout
            if (System.currentTimeMillis() - protocolTimeout > 0) {
                throw new ProtocolConnectionException("receiveDataModbus() response timeout error", TIMEOUT_ERROR);
            }

        } // while(true)

    } // private ResponseData receiveData(RequestData requestData) throws NestedIOException, IOException

    /**
     * @param hhuSignOn hhuSignOn
     */
    public void setHHUSignOn(HHUSignOn hhuSignOn) {

    }

    /**
     * @return
     */
    public HHUSignOn getHhuSignOn() {
        return null;
    }

    /**
     * @throws NestedIOException
     * @throws ProtocolConnectionException
     */
    public void disconnectMAC() throws NestedIOException, ProtocolConnectionException {

    }

    /**
     * @param strID         property MeterProtocol.ADDRESS (DeviceId)
     * @param strPassword   property MetrProtocol.PASSWORD (Password)
     * @param securityLevel custom property "SecurityLevel"
     * @param nodeId        property MeterProtocol.NODEID (NodeAddress)
     * @return
     * @throws IOException
     * @throws ProtocolConnectionException
     */
    public MeterType connectMAC(String strID, String strPassword, int securityLevel, String nodeId) throws IOException {
        if (strID != null) {
            setAddress(Integer.parseInt(strID));
        } else {
            throw new IOException("DeviceID invalid! Must have a value! Correct first!");
        }
        return null;
    }

    /**
     * @param strID  property MeterProtocol.ADDRESS (DeviceId)
     * @param nodeId property MeterProtocol.NODEID (NodeAddress)
     * @return
     * @throws NestedIOException
     * @throws ProtocolConnectionException
     */
    public byte[] dataReadout(String strID, String nodeId) throws NestedIOException, ProtocolConnectionException {
        return null;
    }

    /**
     * Getter for the timeout property
     *
     * @return
     */
    public int getTimeout() {
        return timeout;
    }

    /**
     * Getter for the maxRetries property
     *
     * @return
     */
    public int getMaxRetries() {
        return maxRetries;
    }

}
