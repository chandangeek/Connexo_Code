/*
 * ProcedureFactory.java
 *
 * Created on 20 oktober 2005, 11:04
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.procedures;

import com.energyict.protocolimpl.ansi.c12.C12ProtocolLink;

/**
 *
 * @author Koen
 */
abstract public class ProcedureFactory {

    private C12ProtocolLink c12ProtocolLink;
    int sequenceNr;

    /** Creates a new instance of ProcedureFactory */
    public ProcedureFactory(C12ProtocolLink c12ProtocolLink) {
        this.c12ProtocolLink = c12ProtocolLink;
        sequenceNr = 0;
    }

    public C12ProtocolLink getC12ProtocolLink() {
        return c12ProtocolLink;
    }

    protected int getNewSequenceNr() {
        return sequenceNr++;
    }

}
