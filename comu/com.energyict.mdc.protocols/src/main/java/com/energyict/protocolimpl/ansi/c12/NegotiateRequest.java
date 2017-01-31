/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * NegotiateRequest.java
 *
 * Created on 17 oktober 2005, 16:17
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
public class NegotiateRequest extends AbstractRequest {

    RequestData requestData=new RequestData();

    /** Creates a new instance of NegotiateRequest */
    public NegotiateRequest(PSEMServiceFactory psemServiceFactory) {
        super(psemServiceFactory);
    }

    protected void parse(ResponseData responseData) throws IOException {
          response = new NegotiateResponse(getPSEMServiceFactory());
          response.build(responseData);
    }

    protected RequestData getRequestData() {
        return requestData;
    }

    public void negotiate(int packetSize,int nrOfPackets,int baudrateIndex) {
        if (baudrateIndex==0) {
            requestData.setCode(NEGOTIATE);
            requestData.setData(new byte[]{(byte)(packetSize>>8),(byte)packetSize,(byte)nrOfPackets});
        }
        else {
            // KV_TO_DO how and when do we use all other NEGOTIATE_BAUD_x codes?
            requestData.setCode(NEGOTIATE_BAUD_1);
            requestData.setData(new byte[]{(byte)(packetSize>>8),(byte)packetSize,(byte)nrOfPackets,(byte)baudrateIndex});
        }
    }

}