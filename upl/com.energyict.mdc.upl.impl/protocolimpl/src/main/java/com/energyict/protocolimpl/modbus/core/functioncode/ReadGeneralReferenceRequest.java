package com.energyict.protocolimpl.modbus.core.functioncode;

import com.energyict.protocol.ProtocolUtils;
import com.energyict.protocolimpl.modbus.core.connection.RequestData;
import com.energyict.protocolimpl.modbus.core.connection.ResponseData;

import java.io.IOException;

public class ReadGeneralReferenceRequest extends AbstractRequest{
    private static final int DEFAULT_OFFSET = 4;

    private byte[] values = null;

    private RequestData requestData = new RequestData(FunctionCode.READ_GENERAL_REFERENCE.getFunctionCode());

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ReadGeneralReferenceRequest:\n");
        if(getValues() != null) {
            for (int i = 0; i < getValues().length; i++) {
                strBuff.append("register" + i + "=0x" + getValues()[i] + " ");
            }
        }
        return strBuff.toString();
    }

    @Override
    protected void parse(ResponseData responseData) throws IOException {
        int offset = DEFAULT_OFFSET;
        values = ProtocolUtils.getSubArray2(responseData.getData(),offset, responseData.getData().length -offset - 1);
    }

    @Override
    protected RequestData getRequestData() {
        return requestData;
    }

    public ReadGeneralReferenceRequest(FunctionCodeFactory functionCodeFactory) {
        super(functionCodeFactory);
    }

    public byte[] getValues() {
        return values;
    }

    public void setReferenceSpec(long recordNumber) {
        byte[] data = new byte[8];
        data[0] = 0x07;//Byte count for remainder of request (=7 x number of groups)
        data[1] = 0x06;//Reference type for first group = 06 for 6xxxx extended register files
        /*Reference number for first group
        = file number:offset for 6xxxx files
        = 32 bit reference number for 4xxxx registers*/
        long recordNo = 65536 + recordNumber;
        String recordNoString = ProtocolUtils.buildStringHex(recordNo, 3);
        data[2] = 0x00;
        data[3] = Byte.parseByte(recordNoString.substring(0, 1), 16);
        data[4] = Byte.parseByte(recordNoString.substring(1, 3), 16);
        data[5] = Byte.parseByte(recordNoString.substring(3, 5), 16);
        data[6] = 0x00;//Word count for first group
        data[7] = 0x26;//Word count for first group
        requestData.setData(data);
    }
}
