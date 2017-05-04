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
public class DukePower extends AbstractManufacturer {



    /** Creates a new instance of SLB */
    public DukePower() {
    }

    public String getManufacturer() throws IOException {
        return "EnergyICT";
    }

    public String getMeterProtocolClass() throws IOException {
        return "com.energyict.protocolimpl.dukepower.DukePower";
    }

    public String[] getMeterSerialNumberRegisters() throws IOException {
        return null;
    }

    public String getMeterDescription() throws IOException {
        return "EnergyICT datalogger";
    }

}
