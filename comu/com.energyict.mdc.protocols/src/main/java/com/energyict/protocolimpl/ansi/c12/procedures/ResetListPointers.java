/*
 * ResetListPointers.java
 *
 * Created on 19 oktober 2005, 21:04
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.procedures;

import java.io.IOException;

/**
 *
 * @author Koen
 */
public class ResetListPointers extends AbstractProcedure {

    /*
     * 0 reserved
     * 1 event log table 76
     * 2 self read data table 26
     * 3 lp data set1 table 64
     * 4 lp data set2 table 65
     * 5 lp data set3 table 66
     * 6 lp data set4 table 67
     * 7 lp data set1..4 table 64..67
     * 8 history log table 74
     * 9..254 reserved
     * 255 all list except event log table 76
     */
    private int list; // UINT8


    /** Creates a new instance of ResetListPointers */
    public ResetListPointers(ProcedureFactory procedureFactory) {
        super(procedureFactory,new ProcedureIdentification(4));
    }

    protected void prepare() throws IOException {
        setProcedureData(new byte[]{(byte)list});
    }

    public void setList(int list) {
        this.list = list;
    }
}