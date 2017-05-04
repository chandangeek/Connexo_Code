/*
 * Copyright (c) 2017 by Honeywell International Inc. All Rights Reserved
 */

/*
 * EMONIDR08A.java
 *
 * Created on 30 oktober 2005, 15:57
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.meteridentification;

import java.io.IOException;
/**
 *
 * @author Koen
 */
public class EMONIDR08A extends AbstractManufacturer {

    public String getManufacturer() throws IOException {
        return "Emon";
    }

    public String getMeterProtocolClass() throws IOException {
        return "com.energyict.protocolimpl.emon.ez7.EZ7";
    }

    public String[] getMeterSerialNumberRegisters() throws IOException {
        return null;
    }

    public String getMeterDescription() throws IOException {
        return "Emon datalogger";
    }
}
