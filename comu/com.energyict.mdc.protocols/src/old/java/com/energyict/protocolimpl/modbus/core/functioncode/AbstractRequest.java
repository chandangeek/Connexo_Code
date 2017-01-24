/*
 * AbstractRequest.java
 *
 * Created on 19 september 2005, 16:25
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

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
       byte[] data=null;
       ResponseData responseData = functionCodeFactory.getModbus().getModbusConnection().sendRequest(getRequestData());
       parse(responseData);
    }


}
