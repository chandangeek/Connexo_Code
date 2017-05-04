/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * SecurityRequest.java
 *
 * Created on 19 oktober 2005, 14:21
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12;

import java.io.IOException;
/**
 *
 * @author Koen
 */
public class SecurityRequest extends AbstractRequest {

    RequestData requestData=new RequestData();

    /** Creates a new instance of SecurityRequest */
    public SecurityRequest(PSEMServiceFactory psemServiceFactory) {
        super(psemServiceFactory);
    }

    protected void parse(ResponseData responseData) throws IOException {
          response = new SecurityResponse(getPSEMServiceFactory());
          response.build(responseData);
    }

    protected RequestData getRequestData() {
        return requestData;
    }

    public void secure(byte[] pw) throws IOException {
        byte[] password = pw;
        if (password.length != 20) {
            password= new byte[20];
            System.arraycopy(pw,0,password,0,pw.length);
            for (int i=10;i<password.length;i++)
                password[i] = 0x20;
        }
            //throw new IOException("SecurityRequest, secure, password must be exactly 20 characters long!");
        requestData.setCode(SECURITY);
        requestData.setData(password);
    }

}