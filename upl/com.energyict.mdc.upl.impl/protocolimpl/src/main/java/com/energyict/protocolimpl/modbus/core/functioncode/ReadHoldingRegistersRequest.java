/*
 * ReadHoldingRegistersRequest.java
 *
 * Created on 19 september 2005, 16:24
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.core.functioncode;

import java.io.*;
import com.energyict.protocolimpl.modbus.core.connection.*;
import com.energyict.protocolimpl.modbus.core.ModbusException;
import com.energyict.protocol.ProtocolUtils;
/**
 *
 * @author Koen
 */
public class ReadHoldingRegistersRequest extends AbstractRequest {
    
    private RequestData requestData = new RequestData(FunctionCodeFactory.FUNCTIONCODE_READHOLDINGREGISTER);
         
    private int[] registers;
    
    /** Creates a new instance of ReadHoldingRegistersRequest */
    public ReadHoldingRegistersRequest(FunctionCodeFactory functionCodeFactory) {
        super(functionCodeFactory);
    }
    
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ReadHoldingRegistersRequest:\n");
        for (int i=0;i<getRegisters().length;i++) {
            strBuff.append("register"+i+"=0x"+Integer.toHexString(getRegisters()[i])+" ");
        }
        return strBuff.toString(); 
    }
    
    protected void parse(ResponseData responseData) throws IOException {
        if (((responseData.getData().length-1)%2) != 0)
            throw new ModbusException("ReadHoldingRegistersRequest, parse, length error",ModbusException.PARSE_LENGTH_ERROR); 
        else {
            int nrOfRegisters = (responseData.getData().length-1) / 2;
            setRegisters(new int[nrOfRegisters]);
            for (int i=0;i<nrOfRegisters;i++) {
                getRegisters()[i] = ProtocolUtils.getInt(responseData.getData(),i*2+1, 2);
            }
        }
    }

    public void setRegisterSpec(int startingAddress, int quantityOfRegisters) {
        byte[] data = new byte[4];
        data[0] = (byte)(startingAddress/256);
        data[1] = (byte)(startingAddress%256);
        data[2] = (byte)(quantityOfRegisters/256);
        data[3] = (byte)(quantityOfRegisters%256);
        requestData.setData(data);
    }

    public RequestData getRequestData() {
        return requestData;
    }

    public int[] getRegisters() {
        return registers;
    }

    public void setRegisters(int[] registers) {
        this.registers = registers;
    }

}
