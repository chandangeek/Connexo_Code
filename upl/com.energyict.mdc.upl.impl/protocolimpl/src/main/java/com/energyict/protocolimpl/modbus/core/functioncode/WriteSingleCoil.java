package com.energyict.protocolimpl.modbus.core.functioncode;

import com.energyict.protocolimpl.modbus.core.ModbusException;
import com.energyict.protocolimpl.modbus.core.connection.RequestData;
import com.energyict.protocolimpl.modbus.core.connection.ResponseData;
import com.energyict.protocolimpl.utils.ProtocolUtils;

import java.io.IOException;

/**
 * Created by cisac on 11/13/2015.
 */
public class WriteSingleCoil extends AbstractRequest {

    private RequestData requestData = new RequestData(FunctionCode.WRITE_SINGLE_COIL.getFunctionCode());

    int writeCoilAddress;
    int writeCoilValue;
    int readCoilAddress;
    int readCoilValue;

    /**
     * Creates a new instance of AbstractRequest
     *
     * @param functionCodeFactory
     */
    public WriteSingleCoil(FunctionCodeFactory functionCodeFactory) {
        super(functionCodeFactory);
    }

    @Override
    protected void parse(ResponseData responseData) throws IOException {
        readCoilAddress = ProtocolUtils.getInt(responseData.getData(), 0, 2);
        readCoilValue = ProtocolUtils.getInt(responseData.getData(),2, 2);
        if ((readCoilAddress != writeCoilAddress) || (readCoilValue != writeCoilValue)) {
            throw new ModbusException("WriteSingleCoil, parse, write error");
        }
    }

    public void writeCoil(int writeCoilAddress, int writeCoilValue) {
        this.writeCoilAddress =writeCoilAddress;
        this.writeCoilValue =writeCoilValue;
        byte[] data = new byte[4];
        data[0] = (byte)(writeCoilAddress/256);
        data[1] = (byte)(writeCoilAddress%256);
        data[2] = (byte)(writeCoilValue/256);
        data[3] = (byte)(writeCoilValue%256);
        requestData.setData(data);
    }

    @Override
    protected RequestData getRequestData() {
        return requestData;
    }

    public String toString() {
        return "WriteSingleCoil: readCoilAddress="+readCoilAddress+", readCoilValue="+readCoilValue;
    }
}
