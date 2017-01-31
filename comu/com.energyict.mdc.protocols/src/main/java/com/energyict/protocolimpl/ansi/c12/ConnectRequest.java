/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

package com.energyict.protocolimpl.ansi.c12;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ConnectRequest extends AbstractRequest {

    RequestData requestData = new RequestData();

    /**
     * Creates a new instance of LogonRequest
     */
    public ConnectRequest(PSEMServiceFactory psemServiceFactory) {
        super(psemServiceFactory);

    }

    protected void parse(ResponseData responseData) throws IOException {
        response = new ConnectResponse(getPSEMServiceFactory());
        response.build(responseData);
    }

    protected RequestData getRequestData() {
        return requestData;
    }

    public void connect(int userId, byte[] user) throws IOException {
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        ByteArrayOutputStream login = new ByteArrayOutputStream();

        C1222Layer.encodeAndAppendInteger(login, userId);
        login.write(0x00);
        byte[] data = {0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20};
        login.write(data);

        login.write(0x00);
        C1222Layer.encodeAndAppendInteger(login, getPSEMServiceFactory().getC1222Buffer().getRequestParms().getSessionIdleTimeout());

        C1222Layer.encodeAndAppendInteger(result, login.size() + 1);
        result.write(0x50);
        result.write(login.toByteArray());
        requestData.setData(result.toByteArray());
    }
}