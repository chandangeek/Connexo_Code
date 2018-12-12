/*
 * ClearAllPendingTables.java
 *
 * Created on 26 oktober 2005, 11:46
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
public class ClearAllPendingTables extends AbstractProcedure {
    
    /** Creates a new instance of ClearAllPendingTables */
    public ClearAllPendingTables(ProcedureFactory procedureFactory) {
        super(procedureFactory,new ProcedureIdentification(14));
    }
}

