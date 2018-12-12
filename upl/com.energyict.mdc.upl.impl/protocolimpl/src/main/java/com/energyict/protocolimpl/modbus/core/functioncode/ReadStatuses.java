package com.energyict.protocolimpl.modbus.core.functioncode;

import com.energyict.protocolimpl.modbus.core.connection.RequestData;
import com.energyict.protocolimpl.modbus.core.connection.ResponseData;
import com.energyict.protocolimpl.utils.ProtocolTools;

import java.io.IOException;

public class ReadStatuses extends AbstractRequest {

    private final RequestData requestData;

    private byte[] coilStatuses;

    public ReadStatuses(FunctionCodeFactory functionCodeFactory, FunctionCode functionCode) {
        super(functionCodeFactory);
        requestData = new RequestData(functionCode.getFunctionCode());
    }
    
    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ReadStatus:\n");
        for (int i=0;i< getStatuses().length;i++) {
            strBuff.append("Status" + i + "=0x" + Integer.toHexString(getStatuses()[i]) + " ");
        }
        return strBuff.toString(); 
    }

    protected void parse(ResponseData responseData) throws IOException {
        setStatuses(ProtocolTools.getSubArray(responseData.getData(), 1, responseData.getData().length));
    }

    public void setRequestSpecifications(int startingAddress, int quantityOfCoilStatuses) {
        byte[] data = new byte[4];
        data[0] = (byte)(startingAddress/256);
        data[1] = (byte)(startingAddress%256);
        data[2] = (byte)(quantityOfCoilStatuses/256);
        data[3] = (byte)(quantityOfCoilStatuses%256);
        requestData.setData(data);
    }

    public RequestData getRequestData() {
        return requestData;
    }

    public byte[] getStatuses() {
        return coilStatuses;
    }

    public void setStatuses(byte[] coilStatuses) {
        this.coilStatuses = coilStatuses;
    }
}
