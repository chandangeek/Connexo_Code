/*
 * LogonResponse.java
 *
 * Created on 17 oktober 2005, 17:30
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
public class LogonResponse extends AbstractResponse {

    private boolean logon=false;
    
    /** Creates a new instance of LogonResponse */
    public LogonResponse(PSEMServiceFactory psemServiceFactory) {
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
