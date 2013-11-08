package com.energyict.protocolimpl.modbus.flonidan.uniflo1200.connection;

import com.energyict.protocolimpl.modbus.core.connection.RequestData;
import com.energyict.protocolimpl.modbus.core.connection.ResponseData;

import java.io.ByteArrayOutputStream;

/**
 * Copyrights EnergyICT
 * Date: 28-mei-2010
 * Time: 9:25:17
 */
public class ConnectionState {

    public static final int WAIT_FOR_ADDRESS = 0;
    public static final int WAIT_FOR_FUNCTIONCODE = 1;
    public static final int WAIT_FOR_DATA = 2;
    public static final int WAIT_FOR_LENGTH = 3;
    public static final int WAIT_FOR_EXCEPTIONCODE = 4;

    private long protocolTimeout = 0;
    private int kar = 0;
    private int state = WAIT_FOR_ADDRESS;
    private ResponseData responseData = new ResponseData();
    private ByteArrayOutputStream resultDataArrayOutputStream = new ByteArrayOutputStream();
    private ByteArrayOutputStream allDataArrayOutputStream = new ByteArrayOutputStream();
    private int len = 0;
    private int functionErrorCode = 0;
    private RequestData requestData = null;

    /**
     * Default constructor for the ConnectionState object
     */
    public ConnectionState() {
        resultDataArrayOutputStream.reset();
        allDataArrayOutputStream.reset();
    }

    public long getProtocolTimeout() {
        return protocolTimeout;
    }

    public void setProtocolTimeout(long protocolTimeout) {
        this.protocolTimeout = protocolTimeout;
    }

    public int getKar() {
        return kar;
    }

    public void setKar(int kar) {
        this.kar = kar;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public ResponseData getResponseData() {
        return responseData;
    }

    public void setResponseData(ResponseData responseData) {
        this.responseData = responseData;
    }

    public ByteArrayOutputStream getResultDataArrayOutputStream() {
        return resultDataArrayOutputStream;
    }

    public void setResultDataArrayOutputStream(ByteArrayOutputStream resultDataArrayOutputStream) {
        this.resultDataArrayOutputStream = resultDataArrayOutputStream;
    }

    public ByteArrayOutputStream getAllDataArrayOutputStream() {
        return allDataArrayOutputStream;
    }

    public void setAllDataArrayOutputStream(ByteArrayOutputStream allDataArrayOutputStream) {
        this.allDataArrayOutputStream = allDataArrayOutputStream;
    }

    public int getLen() {
        return len;
    }

    public void setLen(int len) {
        this.len = len;
    }

    public int getFunctionErrorCode() {
        return functionErrorCode;
    }

    public void setFunctionErrorCode(int functionErrorCode) {
        this.functionErrorCode = functionErrorCode;
    }

    public RequestData getRequestData() {
        return requestData;
    }

    public void setRequestData(RequestData requestData) {
        this.requestData = requestData;
    }
}
