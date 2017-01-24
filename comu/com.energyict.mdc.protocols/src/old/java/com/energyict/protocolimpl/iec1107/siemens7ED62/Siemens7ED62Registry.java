/*
 * Siemens7ED62Registry.java
 *
 * Created on 5 juli 2004, 18:03
 */

package com.energyict.protocolimpl.iec1107.siemens7ED62;

import com.energyict.mdc.protocol.api.MeterExceptionInfo;
import com.energyict.protocolimpl.iec1107.ProtocolLink;
import com.energyict.protocolimpl.iec1107.vdew.AbstractVDEWRegistry;
import com.energyict.protocolimpl.iec1107.vdew.VDEWRegister;
import com.energyict.protocolimpl.iec1107.vdew.VDEWRegisterDataParse;
/**
 *
 * @author  Koen
 */
public class Siemens7ED62Registry extends AbstractVDEWRegistry {

    /** Creates a new instance of IskraEmecoRegister */
    public Siemens7ED62Registry(MeterExceptionInfo meterExceptionInfo,ProtocolLink protocolLink) {
        super(meterExceptionInfo,protocolLink);
    }

    protected void initRegisters() {
        // These registers cannot be requested using IEC1107.
        // We'll extract time and date from the datadump...
        // By setting the register as CACHED the datadump is used to retrieve the register.
        // compount register build with 'Time' and 'Date' as hhmmssYYMMDD
        // Because the datadump identifies the registers with a number, it is necessary to
        // add \n before the number because otherwise it can happen that the number is found elsewhere in the datadump.
        registers.put("DateTime", new VDEWRegister("\n200 \n201",VDEWRegisterDataParse.VDEW_TIMEDATE,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.CACHED));
        registers.put("NrOfBillingResets", new VDEWRegister("\n700",VDEWRegisterDataParse.VDEW_STRING,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.CACHED));

        // Only these two registers can be asked using the IEC1107 protocol. All the other registers generate a timeout and can only be requested using ZVEI protocol.
        registers.put("MeterSerialNumber", new VDEWRegister("0.0.0",VDEWRegisterDataParse.VDEW_STRING,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED));
        registers.put("MeterSerialNumber2", new VDEWRegister("0.0.1",VDEWRegisterDataParse.VDEW_STRING,0, -1,null,VDEWRegister.NOT_WRITEABLE,VDEWRegister.NOT_CACHED));
    }
}
