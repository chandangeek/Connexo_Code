/*
 * SnapShotData.java
 *
 * Created on 9 december 2005, 21:04
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.elster.a3.procedures;

import com.energyict.protocolimpl.ansi.c12.procedures.AbstractProcedure;
import com.energyict.protocolimpl.ansi.c12.procedures.ProcedureFactory;
import com.energyict.protocolimpl.ansi.c12.procedures.ProcedureIdentification;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class RemoteCallComplete extends AbstractProcedure {


/*
    mask bits
    b0: outage call
    b1: restoration call
    b2: billing call
    b3: alarm call
    b4: immediate call
    b5-7: Not defined. Invalid.
*/
    private int mask; // 1 byte

    /** Creates a new instance of SnapShotData */
    public RemoteCallComplete(ProcedureFactory procedureFactory) {
        super(procedureFactory,new ProcedureIdentification(12,true));
    }

    protected void prepare() throws IOException {
        setProcedureData(new byte[]{(byte)getMask()});
    }

    public int getMask() {
        return mask;
    }

    public void setMask(int mask) {
        this.mask = mask;
    }

}
