/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * LogonRequest.java
 *
 * Created on 17 oktober 2005, 17:17
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 * @author Koen
 */
public class LogonRequest extends AbstractRequest { 
    
    RequestData requestData=new RequestData();
    
    /** Creates a new instance of LogonRequest */
    public LogonRequest(PSEMServiceFactory psemServiceFactory) {
        super(psemServiceFactory);
    }
    
    protected void parse(ResponseData responseData) throws IOException {
          response = new LogonResponse(getPSEMServiceFactory());
          response.build(responseData);
    }
    
    protected RequestData getRequestData() {
        return requestData;
    }
    
    public void logon(String password) throws IOException
    {
    	ByteArrayOutputStream result = new ByteArrayOutputStream();

   		result.write(C1222Layer.encodeInteger(password.length()+1));
   		result.write(0x51);
   		result.write(password.getBytes());
        requestData.setData(result.toByteArray());
    }

    public void logon(int userId, byte[] user) throws IOException {
        if (user.length > 10) 
            throw new IOException("LogonRequest, logon, user name is too long (max 10 characters)!");
        requestData.setCode(LOGON);
        byte[] data = new byte[12];
        for (int i=0;i<data.length;i++)
            data[i]=0x20;
        System.arraycopy(user,0,data,2,user.length);
        data[0] = (byte)(userId>>8);
        data[1] = (byte)(userId);
        requestData.setData(data);
    }

}