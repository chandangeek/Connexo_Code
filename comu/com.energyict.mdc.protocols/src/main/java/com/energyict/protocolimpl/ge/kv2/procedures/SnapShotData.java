/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * SnapShotData.java
 *
 * Created on 9 december 2005, 21:04
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.ge.kv2.procedures;

import com.energyict.protocolimpl.ansi.c12.procedures.AbstractProcedure;
import com.energyict.protocolimpl.ansi.c12.procedures.ProcedureFactory;
import com.energyict.protocolimpl.ansi.c12.procedures.ProcedureIdentification;

/**
 *
 * @author Koen
 */
public class SnapShotData extends AbstractProcedure {

    /** Creates a new instance of SnapShotData */
    public SnapShotData(ProcedureFactory procedureFactory) {
        super(procedureFactory,new ProcedureIdentification(84,true));
    }
}
