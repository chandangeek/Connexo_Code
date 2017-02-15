/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * SLB.java
 *
 * Created on 15 april 2005, 11:53
 */

package com.energyict.protocolimpl.meteridentification;

import java.io.IOException;


/**
 *
 * @author  Koen
 */
public class EictVDEW extends AbstractManufacturer {



    /** Creates a new instance of SLB */
    public EictVDEW() {
    }

    public String getManufacturer() throws IOException {
        return "EnergyICT";
    }

    public String getMeterProtocolClass() throws IOException {
        return "com.energyict.protocolimpl.iec1107.eictrtuvdew.EictRtuVdew";
    }

    public String[] getMeterSerialNumberRegisters() throws IOException {
        return null;
    }

    public String getMeterDescription() throws IOException {
        return "EnergyICT datalogger";
    }

}
