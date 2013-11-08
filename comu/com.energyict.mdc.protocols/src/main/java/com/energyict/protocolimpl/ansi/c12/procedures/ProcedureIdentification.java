/*
 * ProcedureIdentification.java
 *
 * Created on 20 oktober 2005, 11:06
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ansi.c12.procedures;

/**
 *
 * @author Koen
 */
public class ProcedureIdentification {
    
    private int procedureNr;
    private boolean stdVsMfgFlag;  
    
    /** Creates a new instance of ProcedureIdentification */
    public ProcedureIdentification(int procedureNr) {
        this(procedureNr,false);
    }
    public ProcedureIdentification(int procedureNr,boolean stdVsMfgFlag) {
        this.procedureNr=procedureNr;
        this.stdVsMfgFlag=stdVsMfgFlag;
    }

    public int getProcedureNr() {
        return procedureNr;
    }

    public boolean isStdVsMfgFlag() {
        return stdVsMfgFlag;
    }
    
}
