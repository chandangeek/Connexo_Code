package com.energyict.protocolimpl.ansi.c12;

import java.io.IOException;

public class ConnectResponse extends AbstractResponse {

    private boolean logon = false;

    /**
     * Creates a new instance of LogonResponse
     */
    public ConnectResponse(PSEMServiceFactory psemServiceFactory) {
        super(psemServiceFactory);
    }

    protected void parse(ResponseData responseData) throws IOException {
        // in case of <ok>
        setLogon(true);
    }

    public boolean isLogon() {
        return logon;
    }

    public void setLogon(boolean logon) {
        this.logon = logon;
    }
}