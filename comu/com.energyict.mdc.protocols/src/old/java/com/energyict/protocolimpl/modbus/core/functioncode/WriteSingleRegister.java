/*
 * WriteSingleRegister.java
 *
 * Created on 20 september 2005, 11:04
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.core.functioncode;

import com.energyict.protocols.util.ProtocolUtils;
import com.energyict.protocolimpl.modbus.core.ModbusException;
import com.energyict.protocolimpl.modbus.core.connection.RequestData;
import com.energyict.protocolimpl.modbus.core.connection.ResponseData;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class WriteSingleRegister extends AbstractRequest {

    private RequestData requestData = new RequestData(FunctionCodeFactory.FUNCTIONCODE_WRITESINGLEREGISTER);

    int writeRegisterAddress;
    int writeRegisterValue;
    int readRegisterAddress;
    int readRegisterValue;

    /** Creates a new instance of WriteSingleRegister */
    public WriteSingleRegister(FunctionCodeFactory functionCodeFactory) {
        super(functionCodeFactory);
    }

    public String toString() {
        return "WriteSingleRegister: readRegisterAddress="+readRegisterAddress+", readRegisterValue="+readRegisterValue;
    }

    protected void parse(ResponseData responseData) throws IOException {
        readRegisterAddress = ProtocolUtils.getInt(responseData.getData(),0, 2);
        readRegisterValue = ProtocolUtils.getInt(responseData.getData(),2, 2);
        if ((readRegisterAddress != writeRegisterAddress) || (readRegisterValue != writeRegisterValue)) {
            throw new ModbusException("WriteSingleRegister, parse, write error",ModbusException.WRITE_ERROR);
        }

    }

    public void writeRegister(int writeRegisterAddress, int writeRegisterValue) {
        this.writeRegisterAddress=writeRegisterAddress;
        this.writeRegisterValue=writeRegisterValue;
        byte[] data = new byte[4];
        data[0] = (byte)(writeRegisterAddress/256);
        data[1] = (byte)(writeRegisterAddress%256);
        data[2] = (byte)(writeRegisterValue/256);
        data[3] = (byte)(writeRegisterValue%256);
        requestData.setData(data);
    }

    public RequestData getRequestData() {
        return requestData;
    }

}
