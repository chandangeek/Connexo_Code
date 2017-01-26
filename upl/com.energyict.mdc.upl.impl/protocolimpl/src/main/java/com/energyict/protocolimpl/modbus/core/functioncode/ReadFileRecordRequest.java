package com.energyict.protocolimpl.modbus.core.functioncode;

import com.energyict.protocolimpl.modbus.core.connection.RequestData;
import com.energyict.protocolimpl.modbus.core.connection.ResponseData;
import com.energyict.protocolimpl.utils.ProtocolTools;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

public class ReadFileRecordRequest extends AbstractRequest {

    private static final int FILE_REFERENCE_TYPE = 0x06;

    private byte[] values = null;

    private RequestData requestData = new RequestData(FunctionCode.READ_FILE_RECORD.getFunctionCode());

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ReadFileRecordRequest:\n");
        if (getValues() != null) {
            for (int i = 0; i < getValues().length; i++) {
                strBuff.append("register" + i + "=0x" + getValues()[i] + " ");
            }
        }
        return strBuff.toString();
    }

    @Override
    protected void parse(ResponseData responseData) throws IOException {
        // Skip response data length
        // Skip file data length
        // Skip file reference type
        int offset = 3;
        values = ProtocolUtils.getSubArray2(responseData.getData(), offset, responseData.getData().length - offset);
    }

    @Override
    protected RequestData getRequestData() {
        return requestData;
    }

    public ReadFileRecordRequest(FunctionCodeFactory functionCodeFactory) {
        super(functionCodeFactory);
    }

    public byte[] getValues() {
        return values;
    }

    public void setFileRecordParameters(int fileNumber, int recordNumber, int recordLength) throws IOException {
        byte[] data = new byte[8];
        data[0] = 0x07; // Byte count for remainder of request
        data[1] = FILE_REFERENCE_TYPE; // Reference type for first group = 06 for 6xxxx extended register files

        ProtocolUtils.arrayCopy(ProtocolTools.getBytesFromInt(fileNumber, 2), data, 2);     // File number
        ProtocolUtils.arrayCopy(ProtocolTools.getBytesFromInt(recordNumber, 2), data, 4);   // Record number
        ProtocolUtils.arrayCopy(ProtocolTools.getBytesFromInt(recordLength, 2), data, 6);   // Record length
        requestData.setData(data);
    }
}
