/*
 * LogoffResponse.java
 *
 * Created on 19 oktober 2005, 9:46
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
public class LogoffResponse extends AbstractResponse {

    private boolean logoff=false;

    /** Creates a new instance of LogoffResponse */
    public LogoffResponse(PSEMServiceFactory psemServiceFactory) {
        super(psemServiceFactory);
    }

    protected void parse(ResponseData responseData) throws IOException {
        // in case of <ok>
        setLogoff(true);
    }

    public boolean isLogoff() {
        return logoff;
    }

    public void setLogoff(boolean logoff) {
        this.logoff = logoff;
    }

}
