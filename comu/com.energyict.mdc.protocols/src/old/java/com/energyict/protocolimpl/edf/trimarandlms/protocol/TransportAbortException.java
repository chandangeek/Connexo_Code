/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * DatalinkAbortException.java
 *
 * Created on 7 december 2006, 16:18
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimarandlms.protocol;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class TransportAbortException extends IOException {



    private int errorNr;
    private boolean dlError;

    public String toString() {
        return "DatalinkAbortException: "+getErrorNr();
    }



    public TransportAbortException(int errorNr) {
        this(null,errorNr,false);
    } // public DatalinkAbortException(String str)

    public TransportAbortException(String str, int errorNr) {
        this(str,errorNr,false);
    }

    public TransportAbortException(int errorNr, boolean dlError) {
        this(null,errorNr,dlError);
    }

    public TransportAbortException(String str, int errorNr, boolean dlError) {
        super(str);
        this.setDlError(dlError);
        setErrorNr(errorNr);

    } // public DatalinkAbortException(String str)



    public int getErrorNr() {
        return errorNr;
    }

    public void setErrorNr(int errorNr) {
        this.errorNr = errorNr;
    }


    public boolean isDlError() {
        return dlError;
    }

    public void setDlError(boolean dlError) {
        this.dlError = dlError;
    }
}
