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
public class DatalinkAbortException extends IOException {



    private int errorNr;
    private boolean phyError;

    public String toString() {
        return "DatalinkAbortException: "+getErrorNr();
    }



    public DatalinkAbortException(int errorNr) {
        this(null,errorNr,false);
    } // public DatalinkAbortException(String str)

    public DatalinkAbortException(String str, int errorNr) {
        this(str,errorNr,false);
    }

    public DatalinkAbortException(int errorNr, boolean phyError) {
        this(null,errorNr,phyError);
    }

    public DatalinkAbortException(String str, int errorNr, boolean phyError) {
        super(str);
        this.setPhyError(phyError);
        setErrorNr(errorNr);

    } // public DatalinkAbortException(String str)



    public int getErrorNr() {
        return errorNr;
    }

    public void setErrorNr(int errorNr) {
        this.errorNr = errorNr;
    }

    public boolean isPhyError() {
        return phyError;
    }

    public void setPhyError(boolean phyError) {
        this.phyError = phyError;
    }
}
