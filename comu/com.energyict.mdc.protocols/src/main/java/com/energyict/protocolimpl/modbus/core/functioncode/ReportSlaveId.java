/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ReportSlaveId.java
 *
 * Created on 20 september 2005, 14:16
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.modbus.core.functioncode;

import com.energyict.protocols.util.ProtocolUtils;

import com.energyict.protocolimpl.modbus.core.connection.RequestData;
import com.energyict.protocolimpl.modbus.core.connection.ResponseData;

import java.io.IOException;
/**
 *
 * @author Koen
 */
public class ReportSlaveId extends AbstractRequest {

    private RequestData requestData = new RequestData(FunctionCodeFactory.FUNCTIONCODE_REPORTSLAVEID);

    private int slaveId;
    private boolean run;
    private byte[] additionalData;

    /** Creates a new instance of ReportSlaveId */
    public ReportSlaveId(FunctionCodeFactory functionCodeFactory) {
        super(functionCodeFactory);
    }

    public String toString() {
        // Generated code by ToStringBuilder
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ReportSlaveId:\n");
        strBuff.append("   additionalData="+ProtocolUtils.outputHexString(getAdditionalData())+"\n");
        strBuff.append("   additionalData (as string)="+new String(getAdditionalData())+"\n");
        strBuff.append("   run="+isRun()+"\n");
        strBuff.append("   slaveId="+getSlaveId()+"\n");
        return strBuff.toString();
    }

    protected void parse(ResponseData responseData) throws IOException {
        int offset = 1;
        byte[] data = responseData.getData();
        setSlaveId(ProtocolUtils.getInt(data,offset++,1));
        setRun((ProtocolUtils.getInt(data,offset++,1)==0xFF));
        setAdditionalData(ProtocolUtils.getSubArray(data,offset));
    }

    public RequestData getRequestData() {
        return requestData;
    }

    public int getSlaveId() {
        return slaveId;
    }

    public void setSlaveId(int slaveId) {
        this.slaveId = slaveId;
    }

    public boolean isRun() {
        return run;
    }

    public void setRun(boolean run) {
        this.run = run;
    }

    public byte[] getAdditionalData() {
        return additionalData;
    }
    public String getAdditionalDataAsString() {
        return new String(additionalData);
    }

    public void setAdditionalData(byte[] additionalData) {
        this.additionalData = additionalData;
    }
}
