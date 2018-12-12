package com.energyict.protocolimpl.modbus.core.functioncode;

import com.energyict.protocolimpl.modbus.core.connection.RequestData;
import com.energyict.protocolimpl.modbus.core.connection.ResponseData;

import java.io.IOException;

/**
 *
 * @author Koen
 */
abstract public class AbstractRequest {
    
    FunctionCodeFactory functionCodeFactory;
            
    abstract protected void parse(ResponseData responseData) throws IOException;
    abstract protected RequestData getRequestData();
    
    /** Creates a new instance of AbstractRequest */
    public AbstractRequest(FunctionCodeFactory functionCodeFactory) {
        this.functionCodeFactory=functionCodeFactory;
    }
    
    protected void prepareBuild() {
        
    }
    
    public void build() throws IOException {
       prepareBuild();
       ResponseData responseData = functionCodeFactory.getModbus().getModbusConnection().sendRequest(getRequestData());
       parse(responseData);
    }
}
