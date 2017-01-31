/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

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

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class StandardProcedureFactory extends ProcedureFactory {


    /** Creates a new instance of StandardProcedureFactory */
    public StandardProcedureFactory(C12ProtocolLink c12ProtocolLink) {
        super(c12ProtocolLink);
    }

    public void setDateTime() throws IOException {
        SetDateTime sdt = new SetDateTime(this);
        sdt.setSequenceNr(getNewSequenceNr());
        sdt.initiateProcedure();
    }

}
