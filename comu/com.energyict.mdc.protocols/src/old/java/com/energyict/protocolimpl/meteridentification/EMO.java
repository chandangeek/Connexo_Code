/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * EMO.java
 *
 * Created on 15 april 2005, 11:53
 */

package com.energyict.protocolimpl.meteridentification;

import java.io.IOException;


/**
 *
 * @author  Koen
 */
public class EMO extends AbstractManufacturer {

    /** Creates a new instance of EMO */
    public EMO() {
    }

    public String getManufacturer() throws IOException {
        return "Enermet";
    }

    public String getMeterProtocolClass() throws IOException {
        return "com.energyict.protocolimpl.iec1107.enermete70x.EnermetE70X";
    }

    public String[] getMeterSerialNumberRegisters() throws IOException {
        return new String[]{"C.1.0"};
    }

    public String getMeterDescription() throws IOException {
        return "Enermet E70x IEC1107";
    }

}
