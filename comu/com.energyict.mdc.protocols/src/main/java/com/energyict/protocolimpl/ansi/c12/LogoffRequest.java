/*
 * LogoffRequest.java
 *
 * Created on 19 oktober 2005, 9:45
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
public class LogoffRequest extends AbstractRequest {

    RequestData requestData=new RequestData(LOGOFF);

    /** Creates a new instance of LogoffRequest */
    public LogoffRequest(PSEMServiceFactory psemServiceFactory) {
        super(psemServiceFactory);
    }

    protected void parse(ResponseData responseData) throws IOException {
          response = new LogoffResponse(getPSEMServiceFactory());
          response.build(responseData);
    }

    protected RequestData getRequestData() {
        return requestData;
    }

}
