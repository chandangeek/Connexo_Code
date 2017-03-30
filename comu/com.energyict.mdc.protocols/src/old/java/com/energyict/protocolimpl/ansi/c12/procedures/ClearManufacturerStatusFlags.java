/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ClearManufacturerStatusFlags.java
 *
 * Created on 26 oktober 2005, 10:24
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
public class ClearManufacturerStatusFlags extends AbstractProcedure {

    /** Creates a new instance of ClearManufacturerStatusFlags */
    public ClearManufacturerStatusFlags(ProcedureFactory procedureFactory) {
        super(procedureFactory,new ProcedureIdentification(8));
    }

    protected void prepare() throws IOException {
        setProcedureData(getProcedureFactory().getC12ProtocolLink().getStandardTableFactory().getEndDeviceModeAndStatusTable().getEndDeviceManufacturerStatus());
    }
}
