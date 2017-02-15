/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ReadDeviceIdentification.java
 *
 * Created on 21 september 2005, 11:12
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
import java.util.ArrayList;
import java.util.List;
/**
 *
 * @author Koen
 */
public class ReadDeviceIdentification extends AbstractRequest {

    private RequestData requestData = new RequestData(FunctionCodeFactory.FUNCTIONCODE_READDEVICEID);

    private static final int MEI_CODE_MODUS=14;

    int conformityLevel;
    boolean moreFollows;
    int nextObjectId;
    int nrOfObjects;
    private List deviceObjects=null;
    int readDeviceId;
    int meiType;

    /** Creates a new instance of ReadDeviceIdentification */
    public ReadDeviceIdentification(FunctionCodeFactory functionCodeFactory) {
        super(functionCodeFactory);
    }

    public String toString() {
        StringBuffer strBuff = new StringBuffer();
        strBuff.append("ReadDeviceIdentification:\n");
        strBuff.append("conformityLevel="+conformityLevel+", moreFollows="+moreFollows+", nextObjectId="+nextObjectId+", nrOfObjects="+nrOfObjects+"\n");
        for (int i=0;i<nrOfObjects;i++) {
            strBuff.append("deviceObject"+i+"="+getDeviceObjects().get(i));
        }


        return strBuff.toString();
    }

    protected void parse(ResponseData responseData) throws IOException {
        int offset=0;
        byte[] data = responseData.getData();

        meiType = ProtocolUtils.getInt(data,offset++,1);
        if (meiType != 14) // mei type 14 is the device identification
            throw new IOException("ReadDeviceIdentification, parse, invalid meiType "+meiType);
        readDeviceId = ProtocolUtils.getInt(data,offset++,1);
        if (readDeviceId != 1) // readDeviceId 1 are the mandatory objects
            throw new IOException("ReadDeviceIdentification, parse, invalid readDeviceId "+readDeviceId);
        conformityLevel = ProtocolUtils.getInt(data,offset++,1);
        moreFollows = ProtocolUtils.getInt(data,offset++,1)==0xFF?true:false;
        nextObjectId = ProtocolUtils.getInt(data,offset++,1);
        nrOfObjects = ProtocolUtils.getInt(data,offset++,1);
        int length=0;
        for (int i=0;i<nrOfObjects;i++) {
            if (getDeviceObjects() == null)
                setDeviceObjects(new ArrayList());
            int id = ProtocolUtils.getInt(data,offset++,1);
            int len = ProtocolUtils.getInt(data,offset++,1);
            getDeviceObjects().add(new DeviceObject(id,len, ProtocolUtils.getSubArray2(responseData.getData(),offset,len)));
            offset+=len;
        }
    }

    public void setDeviceIdSpec(int readDeviceIdCode, int objectID) {
        byte[] data = new byte[3];
        data[0] = (byte)(MEI_CODE_MODUS);
        data[1] = (byte)(readDeviceIdCode);
        data[2] = (byte)(objectID);
        requestData.setData(data);
    }

    public RequestData getRequestData() {
        return requestData;
    }

    public List getDeviceObjects() {
        return deviceObjects;
    }

    public void setDeviceObjects(List deviceObjects) {
        this.deviceObjects = deviceObjects;
    }
}
