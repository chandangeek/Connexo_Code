package com.energyict.protocolimpl.modbus.core.functioncode;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.modbus.core.ModbusException;
import com.energyict.protocolimpl.modbus.core.connection.RequestData;
import com.energyict.protocolimpl.modbus.core.connection.ResponseData;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ReadHoldingRegistersRequest extends AbstractRequest {
    
    private RequestData requestData = new RequestData(FunctionCode.READ_HOLDING_REGISTER.getFunctionCode());
         
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
            throw new ModbusException("ReadHoldingRegistersRequest.parse(): Failed to parse the response - response data has invalid length.");
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