/*
 * SPDUFactory.java
 *
 * Created on 19 juni 2006, 16:55
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.edf.trimaran.core;

import com.energyict.protocolimpl.edf.trimaran.Trimaran;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class SPDUFactory {

    private Trimaran trimeran;

    /** Creates a new instance of SPDUFactory */
    public SPDUFactory(Trimaran trimeran) {
        this.setTrimeran(trimeran);
    }

    public Trimaran getTrimeran() {
        return trimeran;
    }

    private void setTrimeran(Trimaran trimeran) {
        this.trimeran = trimeran;
    }

    public void logon() throws IOException {
        XID xid = new XID(this);
        xid.setSlavePassword(Integer.parseInt(getTrimeran().getInfoTypePassword()));
        xid.invoke();

    }
    public void logoff() throws IOException {
        EOS eos = new EOS(this);
        eos.invoke();
    }

    public ENQ enq(int code, int len) throws IOException {
        ENQ enq = new ENQ(this);
        enq.setCode(code);
        enq.setLength(len);
        enq.invoke();
        return enq;
    }
}
