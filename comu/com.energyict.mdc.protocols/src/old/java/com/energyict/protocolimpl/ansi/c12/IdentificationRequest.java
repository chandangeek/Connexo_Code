/*
 * IdentificationRequest.java
 *
 * Created on 16 oktober 2005, 17:22
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
public class IdentificationRequest extends AbstractRequest {

    RequestData requestData=new RequestData(IDENTIFICATION);

    /** Creates a new instance of IdentificationRequest */
    public IdentificationRequest(PSEMServiceFactory psemServiceFactory) {
        super(psemServiceFactory);
    }

    protected void parse(ResponseData responseData) throws IOException {
          response = new IdentificationResponse(getPSEMServiceFactory());
          response.build(responseData);
    }

    protected RequestData getRequestData() {
        return requestData;
    }

}
