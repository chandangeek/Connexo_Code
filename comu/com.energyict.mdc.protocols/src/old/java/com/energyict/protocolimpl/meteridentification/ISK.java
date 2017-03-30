/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * ISK.java
 *
 * Created on 15 april 2005, 13:12
 */

package com.energyict.protocolimpl.meteridentification;

import java.io.IOException;


/**
 *
 * @author  Koen
 */
public class ISK extends AbstractManufacturer {

    /** Creates a new instance of ISK */
    public ISK() {
    }

    public String getManufacturer() throws IOException {
        return "Iskra Emeco";
    }

    public String getMeterProtocolClass() throws IOException {
        return "com.energyict.protocolimpl.iec1107.iskraemeco.IskraEmeco";
    }

    public String[] getMeterSerialNumberRegisters() throws IOException {
        return new String[]{"00"};
    }

    public String getMeterDescription() throws IOException {
        return "IEC1107 FLAG IskraEmeco VDEW";
    }

}
