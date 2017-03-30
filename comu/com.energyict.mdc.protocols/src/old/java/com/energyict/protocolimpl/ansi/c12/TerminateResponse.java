/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * TerminateResponse.java
 *
 * Created on 19 oktober 2005, 14:12
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
public class TerminateResponse extends AbstractResponse {

    private boolean terminate=false;

    /** Creates a new instance of TerminateResponse */
    public TerminateResponse(PSEMServiceFactory psemServiceFactory) {
        super(psemServiceFactory);
    }

    protected void parse(ResponseData responseData) throws IOException {
        // in case of <ok>
        setTerminate(true);
    }

    public boolean isTerminate() {
        return terminate;
    }

    public void setTerminate(boolean terminate) {
        this.terminate = terminate;
    }

}
