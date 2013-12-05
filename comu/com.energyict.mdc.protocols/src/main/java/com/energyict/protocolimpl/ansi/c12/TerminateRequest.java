/*
 * TerminateRequest.java
 *
 * Created on 19 oktober 2005, 14:11
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
public class TerminateRequest extends AbstractRequest {

    RequestData requestData=new RequestData(TERMINATE);

    /** Creates a new instance of TerminateRequest */
    public TerminateRequest(PSEMServiceFactory psemServiceFactory) {
        super(psemServiceFactory);
    }

    protected void parse(ResponseData responseData) throws IOException {
          response = new TerminateResponse(getPSEMServiceFactory());
          response.build(responseData);
    }

    protected RequestData getRequestData() {
        return requestData;
    }

}