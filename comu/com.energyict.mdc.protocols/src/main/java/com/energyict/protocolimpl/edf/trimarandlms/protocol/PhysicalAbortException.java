/*
 * PhysicalAbortException.java
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
public class PhysicalAbortException extends IOException {

    private int errorNr;

    public String toString() {
        return "PhysicalAbortException: "+getErrorNr();
    }

    public PhysicalAbortException(String str, int errorNr) {
        super(str);
        setErrorNr(errorNr);
    } // public PhysicalAbortException(String str)

    public PhysicalAbortException(int errorNr) {
        super();
        setErrorNr(errorNr);
    } // public DatalinkAbortException(String str)

    public int getErrorNr() {
        return errorNr;
    }

    public void setErrorNr(int errorNr) {
        this.errorNr = errorNr;
    }
}
