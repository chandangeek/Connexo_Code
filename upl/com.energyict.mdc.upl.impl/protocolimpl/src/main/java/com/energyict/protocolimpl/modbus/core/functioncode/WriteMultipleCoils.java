package com.energyict.protocolimpl.modbus.core.functioncode;

import com.energyict.protocolimpl.modbus.core.ModbusException;
import com.energyict.protocolimpl.modbus.core.connection.RequestData;
import com.energyict.protocolimpl.modbus.core.connection.ResponseData;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/**
 * Created by cisac on 11/13/2015.
 */
public class WriteMultipleCoils extends AbstractRequest{

    private RequestData requestData = new RequestData(FunctionCode.WRITE_MULTIPLE_COILS.getFunctionCode());

    int writeStartingAddress;
    int writeQuantityOfCoils;
    int readStartingAddress;
    int readQuantityOfCoils;

    /**
     * Creates a new instance of AbstractRequest
     *
     * @param functionCodeFactory
     */
    public WriteMultipleCoils(FunctionCodeFactory functionCodeFactory) {
        super(functionCodeFactory);
    }

    @Override
    protected void parse(ResponseData responseData) throws IOException {
        readStartingAddress = ProtocolUtils.getInt(responseData.getData(),0, 2);
        readQuantityOfCoils = ProtocolUtils.getInt(responseData.getData(),2, 2);
        if ((readStartingAddress != writeStartingAddress) || (readQuantityOfCoils != writeQuantityOfCoils)) {
            throw new ModbusException("WriteMultipleCoils, parse, write error");
        }
    }

    public void writeCoil(int writeStartingAddress, int writeQuantityOfCoils, byte[] coilValues) {
        byte[] dataHeader = new byte[5];
        this.writeStartingAddress=writeStartingAddress;
        this.writeQuantityOfCoils=writeQuantityOfCoils;
        dataHeader[0] = (byte)(writeStartingAddress/256);
        dataHeader[1] = (byte)(writeStartingAddress%256);
        dataHeader[2] = (byte)(writeQuantityOfCoils/256);
        dataHeader[3] = (byte)(writeQuantityOfCoils%256);
        dataHeader[4] = (byte)(coilValues.length);
        byte[] data = ProtocolUtils.concatByteArrays(dataHeader, coilValues);
        requestData.setData(data);
    }

    @Override
    protected RequestData getRequestData() {
        return requestData;
    }

    public String toString() {
        return "WriteMultipleCoils: readStartingAddress="+readStartingAddress+", readQuantityOfCoils="+readQuantityOfCoils;
    }
}
