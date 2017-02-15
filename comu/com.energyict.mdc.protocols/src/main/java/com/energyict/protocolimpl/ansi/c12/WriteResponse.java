/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * WriteResponse.java
 *
 * Created on 19 oktober 2005, 16:10
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
public class WriteResponse extends AbstractResponse {

    private boolean written;

    /** Creates a new instance of WriteResponse */
    public WriteResponse(PSEMServiceFactory psemServiceFactory) {
        super(psemServiceFactory);
    }

    public String toString() {
        return null;
    }

    protected void parse(ResponseData responseData) throws IOException {
        // in case of <ok>
        written = true;
    }

    public boolean isWritten() {
        return written;
    }

    public void setWritten(boolean written) {
        this.written = written;
    }

}
