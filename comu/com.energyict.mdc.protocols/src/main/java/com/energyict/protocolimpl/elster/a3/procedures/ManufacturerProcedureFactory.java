/*
 * ManufacturerProcedureFactory.java
 *
 * Created on 9 december 2005, 11:04
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3.procedures;

import com.energyict.protocolimpl.ansi.c12.C12ProtocolLink;
import com.energyict.protocolimpl.ansi.c12.procedures.ProcedureFactory;

import java.io.IOException;
/**
 *
 * @author Koen
 */
public class ManufacturerProcedureFactory extends ProcedureFactory {


    /** Creates a new instance of ManufacturerProcedureFactory */
    public ManufacturerProcedureFactory(C12ProtocolLink c12ProtocolLink) {
        super(c12ProtocolLink);
    }


    public void RemoteCallComplete() throws IOException {
        RemoteCallComplete(0x1F);
    }

    public void RemoteCallComplete(int mask) throws IOException {
        RemoteCallComplete ssd = new RemoteCallComplete(this);
        ssd.setSequenceNr(getNewSequenceNr());
        ssd.setMask(mask);
        ssd.initiateProcedure();
    }

    public CallIdentification getCallIdentification() throws IOException {
        CallIdentification ssd = new CallIdentification(this);
        ssd.setSequenceNr(getNewSequenceNr());
        ssd.initiateProcedure();
        return ssd;
    }

}
