/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * WriteMultipleRegisters.java
 *
 * Created on 21 september 2005, 11:58
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
public class WriteMultipleRegisters extends AbstractRequest {

    private RequestData requestData = new RequestData(FunctionCodeFactory.FUNCTIONCODE_WRITEMULTIPLEREGISTER);

    int writeStartingAddress;
    int writeQuantityOfRegisters;
    int readStartingAddress;
    int readQuantityOfRegisters;

    /** Creates a new instance of WriteMultipleRegisters */
    public WriteMultipleRegisters(FunctionCodeFactory functionCodeFactory) {
        super(functionCodeFactory);
    }

    public String toString() {
        return "WriteMultipleRegisters: readStartingAddress="+readStartingAddress+", readQuantityOfRegisters="+readQuantityOfRegisters;
    }

    protected void parse(ResponseData responseData) throws IOException {
        readStartingAddress = ProtocolUtils.getInt(responseData.getData(),0, 2);
        readQuantityOfRegisters = ProtocolUtils.getInt(responseData.getData(),2, 2);
        if ((readStartingAddress != writeStartingAddress) || (readQuantityOfRegisters != writeQuantityOfRegisters)) {
            throw new ModbusException("WriteSingleRegister, parse, write error",ModbusException.WRITE_ERROR);
        }
    }

    public void writeRegister(int writeStartingAddress, int writeQuantityOfRegisters, byte[] registerValues) {
        byte[] dataHeader = new byte[5];
        this.writeStartingAddress=writeStartingAddress;
        this.writeQuantityOfRegisters=writeQuantityOfRegisters;
        dataHeader[0] = (byte)(writeStartingAddress/256);
        dataHeader[1] = (byte)(writeStartingAddress%256);
        dataHeader[2] = (byte)(writeQuantityOfRegisters/256);
        dataHeader[3] = (byte)(writeQuantityOfRegisters%256);
        dataHeader[4] = (byte)(registerValues.length);
        byte[] data = ProtocolUtils.concatByteArrays(dataHeader, registerValues);
        requestData.setData(data);
    }

    public RequestData getRequestData() {
        return requestData;
    }


}
