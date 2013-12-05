/*
 * ElsterAlphaPlus.java
 *
 * Created on 1 december 2005, 15:11
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package com.energyict.protocolimpl.meteridentification;

import java.io.IOException;

/**
 *
 * @author koen
 * Discovery of the original Alpha meter returns the same string as the AlphaPlus. No problem because the read of the SERIALNUMBER
 * can be the same for AlphaPlus as Alpha original...
 *
 */
public class ElsterAlphaPlus extends AbstractManufacturer {

    public String getManufacturer() throws IOException {
        return "Elster";
    }

    public String getMeterProtocolClass() throws IOException {
        return "com.energyict.protocolimpl.elster.alpha.alphaplus.AlphaPlus";
    }

    public String[] getMeterSerialNumberRegisters() throws IOException {
        return null;
    }

    public String getMeterDescription() throws IOException {
        return "PowerPlus Alpha";
    }

    public String getResourceName() throws IOException {
        return "AlphaPlusPasswords";
    }

}
