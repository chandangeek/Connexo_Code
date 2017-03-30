/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ActivateAllPendingTables.java
 *
 * Created on 26 oktober 2005, 10:37
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
public class ActivateAllPendingTables extends AbstractProcedure {
    
    /** Creates a new instance of ActivateAllPendingTables */
    public ActivateAllPendingTables(ProcedureFactory procedureFactory) {
        super(procedureFactory,new ProcedureIdentification(12));
    }
}
