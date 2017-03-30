/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * KamstrupRegister.java
 *
 * Created on 16 juni 2003, 16:35
 */

package com.energyict.protocolimpl.iec1107.kamstrup;

import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.vdew.AbstractVDEWRegistry;
import com.energyict.protocolimpl.iec1107.vdew.VDEWRegister;
import com.energyict.protocolimpl.iec1107.vdew.VDEWRegisterDataParse;


/**
 *
 * @author  Koen
 */
public class KamstrupRegistry extends AbstractVDEWRegistry {

    /** Creates a new instance of KamstrupRegister */
    public KamstrupRegistry(ProtocolLink protocolLink) {
        super(null,protocolLink);
    }

    protected void initRegisters() {
        registers.put("Converter Unconverted volume (Ch.1) Vm1", new VDEWRegister("1:13.0.0",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.CACHED));
        registers.put("Converter Unconverted volume (Ch.2) Vm2", new VDEWRegister("2:13.0.0",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.CACHED));
        registers.put("converter error corrected volume (Ch.1) Vc", new VDEWRegister("13.1.0",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.CACHED));
        registers.put("Converter converted volume (Ch.1) Vb", new VDEWRegister("23.2.0",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.CACHED));
        registers.put("Measured, disturbed volume, Ve", new VDEWRegister("1:12.0.0",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.CACHED));
        registers.put("Measured Temperature", new VDEWRegister("0:41.0.0",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.CACHED));
        registers.put("Measured absolute pressure", new VDEWRegister("0:42.0.0",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.CACHED));
        registers.put("Conversion factor", new VDEWRegister("0:52.0.0",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.CACHED));
        registers.put("Correction factor", new VDEWRegister("0:51.0.0",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.CACHED));
        registers.put("Compressibility factor", new VDEWRegister("0:53.0.0",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.CACHED));
        registers.put("Actual normalised flow 5 minutes avg.", new VDEWRegister("1:43.0.0",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.CACHED));
        registers.put("Actual normalised flow 60 minutes avg.", new VDEWRegister("2:43.0.0",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.CACHED));

        registers.put("Unigas Error code", new VDEWRegister("97.97.0",VDEWRegisterDataParse.VDEW_QUANTITY,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.CACHED));

        registers.put("Time", new VDEWRegister("0.9.1",VDEWRegisterDataParse.VDEW_TIMESTRING,0, -1,null,VDEWRegister.WRITEABLE,VDEWRegister.NOT_CACHED));
        registers.put("Date", new VDEWRegister("0.9.2",VDEWRegisterDataParse.VDEW_DATESTRING,0, -1,null,VDEWRegister.WRITEABLE,VDEWRegister.NOT_CACHED));
        registers.put("TimeDate", new VDEWRegister("0.9.1 0.9.2",VDEWRegisterDataParse.VDEW_TIMEDATE,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED));
        registers.put("1107 device address", new VDEWRegister("C.90.1",VDEWRegisterDataParse.VDEW_STRING,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.CACHED));
        registers.put("UNIGAS software revision number", new VDEWRegister("C.90.2",VDEWRegisterDataParse.VDEW_STRING,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.CACHED));
        registers.put("CI software revision number", new VDEWRegister("C.90.3",VDEWRegisterDataParse.VDEW_STRING,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.CACHED));

        registers.put("actual status bits", new VDEWRegister("C.5",VDEWRegisterDataParse.VDEW_STRING,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.CACHED));
    }



}
